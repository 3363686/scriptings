package liveplugin.testrunner
import com.intellij.execution.*
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.execution.junit2.TestProxy
import com.intellij.execution.junit2.info.TestInfo
import com.intellij.execution.junit2.segments.ObjectReader
import com.intellij.execution.junit2.states.Statistics
import com.intellij.execution.junit2.states.TestState
import com.intellij.execution.junit2.ui.ConsolePanel
import com.intellij.execution.junit2.ui.actions.JUnitToolbarPanel
import com.intellij.execution.junit2.ui.model.CompletionEvent
import com.intellij.execution.junit2.ui.model.JUnitListenersNotifier
import com.intellij.execution.junit2.ui.model.JUnitRunningModel
import com.intellij.execution.junit2.ui.model.TreeCollapser
import com.intellij.execution.junit2.ui.properties.JUnitConsoleProperties
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.BasicProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.Printer
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.ToolbarPanel
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView
import com.intellij.execution.testframework.ui.TestResultsPanel
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.annotations.NotNull

import javax.swing.*

import static com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT
import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT
import static com.intellij.rt.execution.junit.states.PoolOfTestStates.*

class JUnitPanel implements TestReporter {
	private static Class treeConsoleViewClass

	@Delegate private TestProxyUpdater testProxyUpdater
	private ProcessHandler handler
	private JUnitRunningModel model
	private TestProxy rootTestProxy
	private long allTestsStartTime

	def showIn(Project project) {
		def executor = DefaultRunExecutor.getRunExecutorInstance()

		JUnitConfiguration myConfiguration = new JUnitConfiguration("Temp config", project, new JUnitConfigurationType().configurationFactories.first())
		JUnitConsoleProperties consoleProperties = new JUnitConsoleProperties(myConfiguration, executor)

		ConfigurationFactory factory = new JUnitConfigurationType().configurationFactories.first()
		RunnerAndConfigurationSettings runnerAndConfigSettings = RunManager.getInstance(project).createRunConfiguration("Temp run config", factory)
		ExecutionEnvironment myEnvironment = newExecutionEnvironment(executor, new BasicProgramRunner(), runnerAndConfigSettings, (Project) project)

		handler = new ProcessHandler() {
			@Override protected void destroyProcessImpl() { notifyProcessTerminated(0) }
			@Override protected void detachProcessImpl() { notifyProcessDetached() }
			@Override boolean detachIsDefault() { true }
			@Override OutputStream getProcessInput() { new ByteArrayOutputStream() }
		}

		rootTestProxy = new TestProxy(newTestInfo("Integration tests"))
		model = new JUnitRunningModel(rootTestProxy, consoleProperties)
        model.notifier.onFinished() // disable listening for events (see also JUnitListenersNotifier.onEvent)

        def additionalActions = new AppendAdditionalActions(project, "Integration tests")
		def consoleView = newConsoleView(consoleProperties, myEnvironment, rootTestProxy, additionalActions)
		consoleView.initUI()
		consoleView.attachToProcess(handler)
		consoleView.attachToModel(model)

		ExecutionManager.getInstance(project).contentManager.showRunContent(executor, additionalActions.descriptor)

		testProxyUpdater = new TestProxyUpdater(rootTestProxy, model.notifier)
		this
	}

	void startedAllTests(long time) {
		handler.startNotify()
		allTestsStartTime = time
	}

	void finishedAllTests(long time) {
		handler.destroyProcess()

		testProxyUpdater.finished()

		model.notifier.fireRunnerStateChanged(new CompletionEvent(true, time - allTestsStartTime))
	}

	static TestInfo newTestInfo(String name, String comment = "") {
		new TestInfo() {
			@Override String getName() { name }
			@Override String getComment() { comment }
			@Override void readFrom(ObjectReader objectReader) {}
			@IJ13Compatibility /*@Override*/ Location getLocation(Project project, GlobalSearchScope globalSearchScope) { null }
		}
	}

	@SuppressWarnings("GroovyAssignabilityCheck")
	@IJ13Compatibility
	private static ExecutionEnvironment newExecutionEnvironment(Executor executor, ProgramRunner programRunner,
	                                                            RunnerAndConfigurationSettings runnerAndConfigSettings, Project project) {
		if (beforeIJ13()) {
			new ExecutionEnvironment(programRunner, runnerAndConfigSettings, project)
		} else {
			new ExecutionEnvironment(executor, programRunner, runnerAndConfigSettings, project)
		}
	}

	@IJ13Compatibility
	private static BaseTestsOutputConsoleView newConsoleView(JUnitConsoleProperties consoleProperties, ExecutionEnvironment myEnvironment,
	                                                         TestProxy rootTestProxy, AppendAdditionalActions additionalActions) {
		if (beforeIJ13()) {
			new MyTreeConsoleView(consoleProperties, myEnvironment, rootTestProxy, additionalActions)
		} else {
			newTreeConsoleView13(consoleProperties, myEnvironment, rootTestProxy, additionalActions)
		}
	}

	private static boolean beforeIJ13() {
		ApplicationInfo.instance.build.baselineVersion < 130
	}

	private static class TestProxyUpdater {
		private static final runningState = newTestState(RUNNING_INDEX, null, false)
		private static final passedState = newTestState(PASSED_INDEX)
		private static final failedState = newTestState(FAILED_INDEX)
		private static final errorState = newTestState(ERROR_INDEX)
		private static final ignoredState = newTestState(IGNORED_INDEX)

		private final def testProxyByClassName = new HashMap<String, TestProxy>().withDefault{ String className ->
			int i = className.lastIndexOf(".")
			if (i == -1 || i == className.size() - 1) {
				new TestProxy(newTestInfo(className))
			} else {
				def simpleClassName = className[i + 1..-1]
				def classPackage = className[0..i - 1]
				new TestProxy(newTestInfo(simpleClassName, classPackage))
			}
		}
		private final def testProxyByMethodName = new HashMap<String, TestProxy>().withDefault{ methodName ->
			new TestProxy(newTestInfo(methodName))
		}
		private final def testStartTimeByMethodName = new HashMap<String, Long>()
		private final def testStartTimeByClassName = new HashMap<String, Long>()

        private final TestProxy rootTestProxy
        private final JUnitListenersNotifier listenersNotifier

        TestProxyUpdater(TestProxy rootTestProxy, JUnitListenersNotifier listenersNotifier) {
            this.listenersNotifier = listenersNotifier
            this.rootTestProxy = rootTestProxy
		}

		void running(String className, String methodName, long time = System.currentTimeMillis()) {
			def classTestProxy = testProxyByClassName.get(className)
			if (!rootTestProxy.children.contains(classTestProxy)) {

                listenersNotifier.onStarted() // onStarted/onFinished to fix https://github.com/dkandalov/live-plugin/issues/39
                rootTestProxy.addChild(classTestProxy)
                listenersNotifier.onFinished()

                classTestProxy.setState(runningState)
				testStartTimeByClassName.put(className, time)
			}

			def methodTestProxy = testProxyByMethodName.get(methodName)
			if (!classTestProxy.children.contains(methodTestProxy)) {
				classTestProxy.addChild(methodTestProxy)
				methodTestProxy.setState(runningState)
				testStartTimeByMethodName.put(methodName, time)
			}
		}

		void passed(String methodName, long time = System.currentTimeMillis()) {
			testProxyByMethodName.get(methodName).state = passedState
			testProxyByMethodName.get(methodName).statistics = statisticsWithDuration((int) time - testStartTimeByMethodName.get(methodName))
		}

		void failed(String methodName, String error, long time = System.currentTimeMillis()) {
			testProxyByMethodName.get(methodName).state = newTestState(FAILED_INDEX, error)
			testProxyByMethodName.get(methodName).statistics = statisticsWithDuration((int) time - testStartTimeByMethodName.get(methodName))
		}

		void error(String methodName, String error, long time = System.currentTimeMillis()) {
			testProxyByMethodName.get(methodName).state = newTestState(ERROR_INDEX, error)
			testProxyByMethodName.get(methodName).statistics = statisticsWithDuration((int) time - testStartTimeByMethodName.get(methodName))
		}

		void ignored(String methodName) {
			testProxyByMethodName.get(methodName).state = ignoredState
		}

		void finishedClass(String className, long time = System.currentTimeMillis()) {
			def testProxy = testProxyByClassName.get(className)
			def hasChildWith = { int state -> testProxy.children.any{ it.state.magnitude == state } }

			if (hasChildWith(FAILED_INDEX)) testProxy.state = failedState
			else if (hasChildWith(ERROR_INDEX)) testProxy.state = errorState
			else testProxy.state = passedState

			testProxy.statistics = statisticsWithDuration((int) time - testStartTimeByClassName.get(className))
		}

		void finished() {
			def hasChildWith = { state -> rootTestProxy.children.any{ it.state.magnitude == state } }

			if (hasChildWith(FAILED_INDEX)) rootTestProxy.state = failedState
			else if (hasChildWith(ERROR_INDEX)) rootTestProxy.state = errorState
			else rootTestProxy.state = passedState
		}

		private static Statistics statisticsWithDuration(int testMethodDuration) {
			new Statistics() {
				@Override int getTime() { testMethodDuration }
			}
		}

		private static TestState newTestState(int state, String message = null, boolean isFinal = true) {
			new TestState() {
				@Override int getMagnitude() { state }
				@Override boolean isFinal() { isFinal }
				@Override void printOn(Printer printer) {
					if (message != null) {
						def contentType = (state == FAILED_INDEX || state == ERROR_INDEX) ? ERROR_OUTPUT : NORMAL_OUTPUT
						printer.print(message, contentType)
					}
				}
			}
		}
	}


	static class AppendAdditionalActions {
		private final Project project
		private final String consoleTitle
		RunContentDescriptor descriptor

		AppendAdditionalActions(Project project, String consoleTitle) {
			this.project = project
			this.consoleTitle = consoleTitle
		}

		def appendTo(DefaultActionGroup actionGroup, ConsoleView console, JComponent jComponent) {
			descriptor = new RunContentDescriptor(console, null, jComponent, consoleTitle) {
				@Override boolean isContentReuseProhibited() { true }
				@Override Icon getIcon() { AllIcons.Nodes.Plugin }
			}
			def closeAction = new CloseAction(DefaultRunExecutor.runExecutorInstance, descriptor, project)

			actionGroup.addSeparator()
			actionGroup.addAction(closeAction)
		}
	}


	/**
	 * Copy-pasted com.intellij.execution.junit2.ui.JUnitTreeConsoleView in attempt to "reconfigure" ConsolePanel
	 */
	private static class MyTreeConsoleView extends BaseTestsOutputConsoleView {
		private ConsolePanel myConsolePanel;
		private final JUnitConsoleProperties myProperties;
		private final ExecutionEnvironment myEnvironment;
		private final AppendAdditionalActions appendAdditionalActions

		public MyTreeConsoleView(final JUnitConsoleProperties properties,
		                         final ExecutionEnvironment environment,
		                         final AbstractTestProxy unboundOutputRoot,
		                         AppendAdditionalActions appendAdditionalActions) {
			super(properties, unboundOutputRoot);
			myProperties = properties;
			myEnvironment = environment;
			this.appendAdditionalActions = appendAdditionalActions
		}

		protected TestResultsPanel createTestResultsPanel() {
			myConsolePanel = new ConsolePanel(getConsole().getComponent(), getPrinter(), myProperties, myEnvironment, getConsole().createConsoleActions()) {
				@Override protected ToolbarPanel createToolbarPanel() {

					return new JUnitToolbarPanel(myProperties, myEnvironment, getConsole().getComponent()) {
						@Override protected void appendAdditionalActions(DefaultActionGroup defaultActionGroup, TestConsoleProperties testConsoleProperties,
						                                                 ExecutionEnvironment environment, JComponent jComponent) {
							super.appendAdditionalActions(defaultActionGroup, testConsoleProperties, environment, jComponent)

							MyTreeConsoleView.this.appendAdditionalActions.appendTo(defaultActionGroup, getConsole(), jComponent)
						}
					}
				}
			}
			return myConsolePanel;
		}

		public void attachToProcess(final ProcessHandler processHandler) {
			super.attachToProcess(processHandler);
			myConsolePanel.onProcessStarted(processHandler);
		}

		public void dispose() {
			super.dispose();
			myConsolePanel = null;
		}

		@Override
		public JComponent getPreferredFocusableComponent() {
			return myConsolePanel.getTreeView();
		}

		public void attachToModel(@NotNull JUnitRunningModel model) {
			if (myConsolePanel != null) {
				myConsolePanel.getTreeView().attachToModel(model);
				model.attachToTree(myConsolePanel.getTreeView());
				myConsolePanel.setModel(model);
				model.onUIBuilt();
				new TreeCollapser().setModel(model);
			}
		}
	}

	private static BaseTestsOutputConsoleView newTreeConsoleView13(JUnitConsoleProperties consoleProperties, ExecutionEnvironment myEnvironment,
	                                                               TestProxy rootTestProxy, AppendAdditionalActions additionalActions) {
		if (treeConsoleViewClass == null) {
			def s = """
import com.intellij.execution.*
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationPerRunnerSettings
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.junit.JUnitConfiguration
import com.intellij.execution.junit.JUnitConfigurationType
import com.intellij.execution.junit2.TestProxy
import com.intellij.execution.junit2.info.TestInfo
import com.intellij.execution.junit2.segments.ObjectReader
import com.intellij.execution.junit2.states.Statistics
import com.intellij.execution.junit2.states.TestState
import com.intellij.execution.junit2.ui.ConsolePanel
import com.intellij.execution.junit2.ui.actions.JUnitToolbarPanel
import com.intellij.execution.junit2.ui.model.CompletionEvent
import com.intellij.execution.junit2.ui.model.JUnitRunningModel
import com.intellij.execution.junit2.ui.model.TreeCollapser
import com.intellij.execution.junit2.ui.properties.JUnitConsoleProperties
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.BasicProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.Printer
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.ToolbarPanel
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView
import com.intellij.execution.testframework.ui.TestResultsPanel
import com.intellij.execution.testframework.ui.TestsOutputConsolePrinter
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.annotations.NotNull

import liveplugin.testrunner.JUnitPanel.AppendAdditionalActions

import javax.swing.*

class MyTreeConsoleView13 extends BaseTestsOutputConsoleView {
	private ConsolePanel myConsolePanel;
	private final JUnitConsoleProperties myProperties;
	private final ExecutionEnvironment myEnvironment;
	private final AppendAdditionalActions appendAdditionalActions

	public MyTreeConsoleView13(final JUnitConsoleProperties properties,
	                         final ExecutionEnvironment environment,
	                         final AbstractTestProxy unboundOutputRoot,
	                         AppendAdditionalActions appendAdditionalActions) {
		super(properties, unboundOutputRoot);
		myProperties = properties;
		myEnvironment = environment;
		this.appendAdditionalActions = appendAdditionalActions
	}

	class MyConsolePanel extends ConsolePanel {
		MyConsolePanel(JComponent console, TestsOutputConsolePrinter printer, JUnitConsoleProperties properties,
		               ExecutionEnvironment environment, AnAction[] consoleActions) {
			super(console, printer, properties, environment, consoleActions)
		}

		@Override protected ToolbarPanel createToolbarPanel() {
			return new MyJUnitToolbarPanel13(myProperties, myEnvironment, MyConsolePanel.this)
		}
	}

	class MyJUnitToolbarPanel13 extends JUnitToolbarPanel {
		MyJUnitToolbarPanel13(TestConsoleProperties properties, ExecutionEnvironment environment, JComponent parentComponent) {
			super(properties, environment, parentComponent)
		}

		@Override protected void appendAdditionalActions(DefaultActionGroup defaultActionGroup, TestConsoleProperties testConsoleProperties,
		                                                 ExecutionEnvironment environment, JComponent jComponent) {
			super.appendAdditionalActions(defaultActionGroup, testConsoleProperties, (ExecutionEnvironment) environment, (JComponent) jComponent)

			MyTreeConsoleView13.this.appendAdditionalActions.appendTo(defaultActionGroup, getConsole(), jComponent)
		}
	}

	@SuppressWarnings("GroovyAssignabilityCheck")
	protected TestResultsPanel createTestResultsPanel() {
		myConsolePanel = new MyConsolePanel(
				getConsole().getComponent(), getPrinter(), myProperties,
				(ExecutionEnvironment) myEnvironment, (AnAction[]) getConsole().createConsoleActions()
		)
		return myConsolePanel;
	}

	public void attachToProcess(final ProcessHandler processHandler) {
		super.attachToProcess(processHandler);
		myConsolePanel.onProcessStarted(processHandler);
	}

	public void dispose() {
		super.dispose();
		myConsolePanel = null;
	}

	@Override
	public JComponent getPreferredFocusableComponent() {
		return myConsolePanel.getTreeView();
	}

	public void attachToModel(@NotNull JUnitRunningModel model) {
		if (myConsolePanel != null) {
			myConsolePanel.getTreeView().attachToModel(model);
			model.attachToTree(myConsolePanel.getTreeView());
			myConsolePanel.setModel(model);
			model.onUIBuilt();
			new TreeCollapser().setModel(model);
		}
	}
}
"""
			treeConsoleViewClass = new GroovyClassLoader(JUnitPanel.classLoader).parseClass(s)
		}
		treeConsoleViewClass.newInstance(consoleProperties, myEnvironment, rootTestProxy, additionalActions)
	}

}

/**
 * Annotation for "switches" to make code compatible with both IJ12 and IJ13 api
 * (this still seems to be simpler than having two branches/versions of plugin)
 */
@interface IJ13Compatibility {}

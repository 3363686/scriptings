/**
 * Original ConsoleView.showInConsole() builds new console on every call.
 * We want print all output to the same console.
 * Here we make public static Console_ame.console,
 * so subsequent outputs can work via Console_ame.console.print().
 *
 * MyConsolePanel is need because it is private.
 *
 * Made from C:\-w\idea\ame\src\liveplugin\implementation\Console.groovy
 * Created by _ame_ on 12.09.2015 19:26.
*/

import com.intellij.execution.ExecutionManager
import com.intellij.execution.Executor
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.actions.CloseAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import liveplugin.implementation.Console
import liveplugin.implementation.Misc
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import org.apache.commons.lang.StringUtils;

import javax.swing.*
import java.awt.*
import java.util.concurrent.atomic.AtomicReference

import static liveplugin.PluginUtil.invokeOnEDT

public class AmeConsole extends Console{
  public boolean appendCR
  private ConsoleView console2
  public AmeConsole( @Nullable message, String consoleTitle = "", @NotNull Project project,
           ConsoleViewContentType contentType = guessContentTypeOf(message) ){
    appendCR = true
    console2 = showInConsole(message, consoleTitle, project, contentType)
  }
  public void print( @NotNull message, int leftm = 0, ConsoleViewContentType contentType = guessContentTypeOf(message) ){
    console2.print( StringUtils.leftPad("", leftm, ' ') + Misc.asString(message)+(appendCR?'\n':''), contentType )
//    console2.print( Misc.asString(message)+'\n(2)', contentType )
  }
}

/* Commented out since used only once and now is commented out there */
/*
class Console_ame{
  public static ConsoleView console
  static ConsoleView showBuildConsole(@Nullable message, String consoleTitle = "", @NotNull Project project,
                                   ConsoleViewContentType contentType = Console.guessContentTypeOf(message)) {
    AtomicReference<ConsoleView> result = new AtomicReference(null)
    // Use reference for consoleTitle because get groovy Reference class like in this bug http://jira.codehaus.org/browse/GROOVY-5101
    AtomicReference<String> titleRef = new AtomicReference(consoleTitle)

    invokeOnEDT{
      console = TextConsoleBuilderFactory.instance.createBuilder(project).console
      console.print(Misc.asString(message), contentType)

      DefaultActionGroup toolbarActions = new DefaultActionGroup()
      def consoleComponent = new MyConsolePanel(console, toolbarActions)
      RunContentDescriptor descriptor = new RunContentDescriptor(console, null, consoleComponent, titleRef.get()) {
        @Override boolean isContentReuseProhibited() { true }
        @Override Icon getIcon() { AllIcons.Nodes.Plugin }
      }
      Executor executor = DefaultRunExecutor.runExecutorInstance

      toolbarActions.add(new CloseAction(executor, descriptor, project))
      console.createConsoleActions().each{ toolbarActions.add(it) }

      ExecutionManager.getInstance(project).contentManager.showRunContent(executor, descriptor)
      result.set(console)
    }
    result.get()
  }
  private static class MyConsolePanel extends JPanel {
    MyConsolePanel(ExecutionConsole consoleView, ActionGroup toolbarActions) {
      super(new BorderLayout())
      def toolbarPanel = new JPanel(new BorderLayout())
      toolbarPanel.add(ActionManager.instance.createActionToolbar(ActionPlaces.UNKNOWN, toolbarActions, false).component)
      add(toolbarPanel, BorderLayout.WEST)
      add(consoleView.component, BorderLayout.CENTER)
    }
  }
}
*/

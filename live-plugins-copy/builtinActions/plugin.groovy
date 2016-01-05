// https://github.com/dkandalov/live-plugin/wiki/Scripting-a-macros

import com.intellij.execution.*
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.ConsoleInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import org.jetbrains.annotations.NotNull

import static FindActionUtil.*
import static com.intellij.execution.filters.ConsoleInputFilterProvider.INPUT_FILTER_PROVIDERS
import static liveplugin.PluginUtil.*

if (isIdeStartup) return

def searchForActions = false
if (searchForActions) {
  // Example of finding actions:
  show(allActionIds().findAll{ it.toLowerCase().contains("project") })
  show(allActionClasses().findAll{ it.simpleName.toLowerCase().contains("project") })

  // Once the action id or class is found, action can be invoked like this:
  actionById("ActivateProjectToolWindow").actionPerformed(anActionEvent())
  return
}


doInBackground {
  def consoleListener = new ConsoleListener("myConsoleListener")
  consoleListener.listen{ text ->
    if (text.contains("NullPointerException")) {

      invokeOnEDT{ actionById("Stop").actionPerformed(anActionEvent()) }

      show("Oh, nooo!! Someone stopped the process!")
      consoleListener.shutdown()
    }
  }
  invokeOnEDT{ runConfiguration("VeryEnterpriseProjectMain") }
}


// there is no "Run" action for each run configuration, so the only way is to do it in code
def runConfiguration(String configurationName) {
  def settings = RunManager.getInstance(project).allSettings.find{ it.name.contains(configurationName) }
  if (settings == null) {
    show("There is no run configuration: <b>${configurationName}</b>.<br/>" +
      "Please create one or change source code to use some other configuration.")
    return
  }
  ProgramRunnerUtil.executeConfiguration(project, settings, DefaultRunExecutor.runExecutorInstance)
}


class FindActionUtil {
  private static actionManager = ActionManager.instance

  static Collection<String> allActionIds() {
    actionManager.getActionIds("")
  }

  static Collection<Class> allActionClasses() {
    actionManager.getActionIds("").collect{ actionManager.getAction(it).class }
  }

  static String actionIdByClass(Class aClass) {
    allActionIds()
      .collectEntries{ [it, actionManager.getAction(it)] }
      .find{ it.value.class.isAssignableFrom(aClass) }?.key
  }

  static Class actionClassById(String id) {
    actionManager.getAction(id).class
  }

  static AnAction actionById(String id) {
    actionManager.getAction(id)
  }

  static AnAction actionByClass(Class aClass) {
    actionById(actionIdByClass(aClass))
  }

  /**
   * @see http://devnet.jetbrains.com/message/5195728#5195728
   * https://github.com/JetBrains/intellij-community/blob/master/platform/platform-api/src/com/intellij/openapi/actionSystem/ex/CheckboxAction.java#L60
   */
  static anActionEvent(DataContext dataContext = DataManager.instance.dataContext, Presentation templatePresentation = new Presentation()) {
    new AnActionEvent(null, dataContext, ActionPlaces.UNKNOWN, templatePresentation, actionManager, 0)
  }
}


/**
 * @see https://github.com/dkandalov/live-plugin/wiki/Console-filtering
 */
class ConsoleListener {
  private final extensionPoint = Extensions.rootArea.getExtensionPoint(INPUT_FILTER_PROVIDERS)
  private final String id

  ConsoleListener(String id) {
    this.id = id
  }

  def listen(Closure callback) {
    def inputFilterProvider = changeGlobalVar(id) { lastInputFilterProvider ->
      if (lastInputFilterProvider != null && extensionPoint.hasExtension(lastInputFilterProvider)) {
        extensionPoint.unregisterExtension(lastInputFilterProvider)
      }
      def notFilteringListener = new InputFilter() {
        @Override List<Pair<String, ConsoleViewContentType>> applyFilter(String consoleText, ConsoleViewContentType contentType) {
          callback(consoleText)
          null
        }
      }
      new ConsoleInputFilterProvider() {
        @Override InputFilter[] getDefaultFilters(@NotNull Project project) {
          [notFilteringListener]
        }
      }
    }
    extensionPoint.registerExtension(inputFilterProvider)
    this
  }

  def shutdown() {
    def lastInputFilterProvider = removeGlobalVar(id)
    if (lastInputFilterProvider != null && extensionPoint.hasExtension(lastInputFilterProvider)) {
      extensionPoint.unregisterExtension(lastInputFilterProvider)
    }
  }
}

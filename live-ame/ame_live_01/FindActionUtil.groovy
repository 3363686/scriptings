// Extracted from: https://github.com/dkandalov/live-plugin/wiki/Scripting-a-macros
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*

import static liveplugin.PluginUtil.show

class FindActionUtil {
  private static actionManager = ActionManager.instance

  static Collection<String> test() {
//    actionManager.getAction( "q" )
  }

  static Collection<String> allActionIds() {
    actionManager.getActionIds("")
  }

  static Collection<Class> allActionClasses() {
//    actionManager.getActionIds("").collect{ actionManager.getAction(it).class }
    actionManager.getActionIds("").collect{
//      show( it )
      actionManager.getAction(it).class }
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

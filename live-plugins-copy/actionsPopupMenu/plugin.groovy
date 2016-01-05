import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.ui.Messages

import static liveplugin.PluginUtil.*

String keyHello = "alt BACK_QUOTE, 3"
registerAction( "helloPopupAction", keyHello ){AnActionEvent event ->
  def project = event.project
  def popupMenuDescription = [
    "Hello World" : [
      "hello, hello" : { show("hello, hello") },
      "hello, how low?": { show("hello, how low?") },
    ],
    "Open in browser": [
      "Live plugin github": {
        openInBrowser("https://github.com/dkandalov/live-plugin")
      },
      "IntelliJ Architectural Overview": {
        openInBrowser("http://confluence.jetbrains.com/display/IDEADEV/IntelliJ+IDEA+Architectural+Overview")
      }
    ],
    "Execute command": {
      def command = Messages.showInputDialog(project,"Enter a command:","title",null)
      if (command == null) return
      show(execute(command))
    },
    "Enter command": {
//      def command = Messages.showMultilineInputDialog(project,"Enter a command:","title","init",null,null)
      def command = Messages.showInputDialogWithCheckBox( "Enter a command:", "title", "String checkboxText",true,true,null,"init",null)
      if (command.first == null) return
      show("${command.first} is ${command.second.toString(  )}","title")
    },
    "Show current project": { context ->
// note that "event.project" cannot be used here because AnActionEvents are not shareable between swing events
// (in this case showing popup menu and choosing menu item are two different swing events)
      show("project: ${project}")
// you can get AnActionEvent of choosing menu item action as shown below
// (to make this event aware of current project popup menu is invoke like this "showPopupMenu(..., event.dataContext)"
      show("project: ${context.event.project}")
    },
    "-": new AnAction("Run actual action") {
      @Override void actionPerformed(AnActionEvent anActionEvent) {
        show("Running actual action")
      }
    },
    "--": Separator.instance,
    "Edit Popup Menu...": {
      openInEditor(pluginPath + "/plugin.groovy")
    },
    "Hello":{
      runDocumentWriteAction(project){
        def editor = currentEditorIn( project )
        editor.document.text += "\nHello IntelliJ"
      }
    }
  ]
  def popupTitle = "Hello PopupMenu"
  showPopupMenu( popupMenuDescription, popupTitle, event.dataContext )
}
show("Loaded 'helloPopupAction'<br/>Use [$keyHello] to run it")

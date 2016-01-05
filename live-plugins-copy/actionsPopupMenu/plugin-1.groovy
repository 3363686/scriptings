import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator

import static liveplugin.PluginUtil.*

String keyHello = "alt BACK_QUOTE, 3"
registerAction( "helloPopupAction", keyHello ){
  AnActionEvent event ->
    def popupMenuDescription = [
      "Hello World"       : [
        "hello, hello"   : { show( "hello, hello" ) },
        "hello, how low?": { show( "hello, how low?" ) },
      ],
      "Open in browser"   : [
        "Live plugin github"             : {
          openInBrowser( "https://github.com/dkandalov/live-plugin" )
        },
        "IntelliJ Architectural Overview": {
          openInBrowser( "http://confluence.jetbrains.com/display/IDEADEV/IntelliJ+IDEA+Architectural+Overview" )
        },
      ],
      "Execute command"   : {
        def command = showInputDialog( "Enter a command:" )
        if( command == null ) return
        show( execute( command ) )
      },
      ""                  : Separator.instance,
      "Edit Popup Menu...": {
        openInEditor( pluginPath + "/plugin.groovy" )
      },
      "Hello":{
        runDocumentWriteAction(event.project){
          def editor = currentEditorIn( event.project )
          editor.document.text += "\nHello IntelliJ"
        }
      }
    ]
    def popupTitle = "Say hello to..."
    showPopupMenu( popupMenuDescription, popupTitle )
}
show( "Loaded 'helloPopupAction'<br/>Use [$keyHello] to run it" )

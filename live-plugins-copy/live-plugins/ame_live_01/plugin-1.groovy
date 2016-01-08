import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator

import static liveplugin.PluginUtil.*

int c_int_calls = c_int_cnt = 0
String actionId = "ame live list 1"
String actionKey = "alt BACK_QUOTE, 1"

registerAction( actionId, actionKey ){
  AnActionEvent event ->
    def project = event.project
    def popupMenuContent = [
      "Hello":{
        runDocumentWriteAction(project) {
          def editor = currentEditorIn(project)
          editor.document.text += "\nHello IntelliJ"
        }
      },
      "Sort Lines" : {
        runDocumentWriteAction( project ){
          def editor = currentEditorIn( project )
          sa = new AmeSort(  )
          sa.doSort( editor )
        }
      },
      "Action1"    : { show( "a1" ); c_int_calls++ },
      "Action2"    : { show( "msg" ); c_int_calls++ },
      "Counters"   : {
        show( "Menu shown ${ ++c_int_calls } times,<br>Counters ${ ++c_int_cnt } times" )
      },
      ""           : Separator.instance,
      "Show":{
        show( "doSort0", "1", NotificationType.WARNING, "id0" )
        show( "doSort1", "2", NotificationType.INFORMATION, "id1" )
        show( "doSort2", "2", NotificationType.INFORMATION, "id1" )
        show( "doSort3", "3")
        show( "doSort4")
      } ,
      "Edit plugin": { openInEditor( pluginPath + "/plugin.groovy" ) },
      "unregister" : {
        unregisterAction( actionId )
        show( "$actionId unregistered from [$actionKey]." )
      }
    ]
    showPopupMenu( popupMenuContent, actionId, event.dataContext )
}
show( "$actionId assigned to [$actionKey]." )


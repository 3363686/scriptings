import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import util2.Data01

import static liveplugin.PluginUtil.*

// OK
String keyHello = "alt BACK_QUOTE, 2"
registerAction("HelloTextEditorAction2nd", keyHello) { AnActionEvent event ->
  runDocumentWriteAction(event.project) {
    def editor = currentEditorIn(event.project)
    editor.document.text += "\n2nd Hello IntelliJ"
    show( "doSort0" )
    SortAction.doSort( editor )
  }
}
show("Loaded '2nd HelloTextEditorAction'<br/>Use $keyHello to run it")

int c_int_calls = c_int_cnt = 0
String aid = "ame live list"
String key = "alt BACK_QUOTE, 1"
//String msg = "mext"

registerAction( aid, key ){
  AnActionEvent event ->
    def popupMenuContent = [
      "Hello":{
// Works but throws exception "Throwable: cannot share data context between Swing events..."
        runDocumentWriteAction(event.project) {
          def editor = currentEditorIn(event.project)
          editor.document.text += "\nHello IntelliJ"
        }
      },
/*
      "Sort Lines" : {
        runDocumentWriteAction( event.project ){
          show( "doSort0" )
          def editor = currentEditorIn( event.project )
          SortAction.doSort( editor )
        }
      },
*/
      "Action1"    : { show( "a1" ); c_int_calls++ },
      "Action2"    : { show( "msg" ); c_int_calls++ },
      "Counters"   : {
        show( "Menu shown ${ ++c_int_calls } times,<br>Counters ${ ++c_int_cnt } times" )
      },
      ""           : Separator.instance,
      "Edit plugin": { openInEditor( pluginPath + "/plugin.groovy" ) },
      "unregister" : {
        unregisterAction( aid )
        show( "$aid unregistered from [$key]." )
      }
    ]
    showPopupMenu( popupMenuContent, aid )
}
show( "$aid assigned to [$key]." )
show( saMarker )
//SortAction sa = new SortAction()
//show( "${sa.saMarker} #2" )
//show( "${sa.sapMarker()} #2" )
show( SortUtils.suMarker )

// Err;
//show( testData.Data02.p )
//show( testData.Data02.n )
//show( testData.Data02.t )
//show( testData.Data01.p )
//show( testData.Data01.n )
// OK and can rename:
//show( testData.Data02.s )
//show( testData.Data02.f() )
// OK and can't rename class:
show( Data01.s )
//show( testData.Data01.t )
//show( testData.Data01.f() )
//show( Data03.t03 )
// Testing:


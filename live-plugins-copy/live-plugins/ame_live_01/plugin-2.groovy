import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.project.Project
import liveplugin.implementation.Console
import liveplugin.implementation.Misc
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

import static liveplugin.PluginUtil.*

String menuId = "ame live list 1"
String menuKey = "alt BACK_QUOTE, 1"
int printIdx = 0
ConsoleView myConsole
AmeConsole console
ConsoleView printToConsole( @Nullable message, String consoleTitle = "", @NotNull Project project, ConsoleViewContentType contentType = Console.guessContentTypeOf(message) ){
  return showInConsole( message, consoleTitle, project )
}
ConsoleView printToConsole( @NotNull message, @NotNull ConsoleView console, ConsoleViewContentType contentType = Console.guessContentTypeOf(message) ){
/*
  if( console == null ){
    return printToConsole( message, "noname console", project, contentType )
  }
*/
  console.print( Misc.asString(message), contentType )
}

registerAction( menuId, menuKey ){ AnActionEvent event ->
  def project = event.project
  def popupMenuContent = [
    "Sort Lines"    : {
      runDocumentWriteAction( project ){
        def editor = currentEditorIn( project )
        AmeSort.doSort( editor )
      }
    },
    "--"            : Separator.instance,
    "new console 1st"         : {
      console = new AmeConsole("Hello console\n", "new console", project)
      console.appendCR = true
      for( int j = 0; j++ < 2; printIdx++ ){
        console.print("$printIdx. ....")
      }
    },
    "new console next"         : {
      for( j = 0; j++ < 3; printIdx++ ){
        console.print("$printIdx. ....")
      }
    },
    "console 1st"         : {
      myConsole = printToConsole("Hello console\n", "my console", project)
      for( int j = 0; j++ < 2; printIdx++ ){
        printToConsole("$printIdx. ....\n", myConsole)
      }
    },
    "console next"         : {
      for( j = 0; j++ < 3; printIdx++ ){
        printToConsole("$printIdx. ....\n", myConsole)
      }
    },
/*
    "_ame console 1st"         : {
      Console_ame.showBuildConsole("Hello console\n", "my console", project)
      for( j = 0; j++ < 2; printIdx++ ){
        Console_ame.console.print("$printIdx. ....\n", ConsoleViewContentType.NORMAL_OUTPUT)
      }
    },
    "_ame console"         : {
      for( j = 0; j++ < 3; printIdx++ ){
        Console_ame.console.print("$printIdx. ....\n", ConsoleViewContentType.NORMAL_OUTPUT)
      }
    },
*/
    "log + console"         : {
      int i = 0
      for( ; i < 5; i++ ){
        log("$i. Hello IntelliJ")
        showInConsole("$i. Hello console", "my console", project)
        showInConsole("$i. ....", "my console", project)
      }
    },
    "FindActionUtil": {
      AmePluginUtil.showAme( "Searching Classes and Actions that contain \"project\"..." )
      def str = "\n"
      def aids
      def classes = FindActionUtil.allActionClasses().findAll{
        it.simpleName.toLowerCase().contains( "project" )
      }
      Arrays.sort( classes )
      AmePluginUtil.showAme( classes );
      for( aClass in classes ){
        str += aClass.simpleName + ':\n  '
        str += FindActionUtil.actionIdByClass(aClass) + '\n'
//        aids = FindActionUtil.allActionIds().findAll{
//          it.toLowerCase().contains( "project" )
//        }
      }
      show( str, "", NotificationType.INFORMATION, "nobaloon" );
      show( "Found, see Event Log" )
    },
    "Test"          : {
      show( it )
    },
    "Hello"         : {
      runDocumentWriteAction( project ){
        def editor = currentEditorIn( project )
        editor.document.text += "\nHello IntelliJ"
      }
    },
    "Sort Lines 1st": {
      runDocumentWriteAction( project ){
        def editor = currentEditorIn( project )
        sa = new AmeSort()
        sa.doSort( editor )
      }
    },
  ]
  showPopupMenu( popupMenuContent, menuId, event.dataContext )
}
show( "$menuId assigned to [$menuKey].","",NotificationType.INFORMATION,"nobaloon" );

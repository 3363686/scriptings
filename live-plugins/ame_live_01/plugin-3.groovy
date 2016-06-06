import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.Separator

import static liveplugin.PluginUtil.*

String menuId = "ame live list 1"
String menuKey = "alt BACK_QUOTE, 1"
def tstr = '', out
int printIdx = 0
int cIdx = aIdx = 1
AmeConsole consoleActions, consoleClasses, consoleActByCl
Presentation pres

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
      consoleClasses = new AmeConsole("Hello console\n", "new console", project)
      for( int j = 0; j++ < 2; printIdx++ ){
        consoleClasses.print("$printIdx. ....")
      }
    },
    "FindActionUtil": {
      def actionManager = ActionManager.instance
      actionManager.getActionIds("")

      AmePluginUtil.showAme( "Searching Classes and Actions..." )
      consoleActions = new AmeConsole("", "Actions", project)
      Collection<String> actIdArr = FindActionUtil.allActionIds()
      AmePluginUtil.showAme( "Actions found" )
      Collections.sort( actIdArr as List )
      AmePluginUtil.showAme( "Actions sorted" )
      for( actId in actIdArr ){
        tstr = ""
        AnAction anAction = actionManager.getAction( actId )
//        tstr = anAction.shortcutSet.shortcuts.toString(  )
        anAction.shortcutSet.shortcuts.each{
          tstr += it.toString(  ) + " ! "
        }
        out = sprintf( "%4d. %-60s %s", aIdx++, actId, tstr )
        consoleActions.print( out )
        pres = anAction.getTemplatePresentation()
        consoleActions.print( pres.description, 8 )
        consoleActions.print( pres.textWithMnemonic, 8 )
      }
      AmePluginUtil.showAme( "Actions printed" )
      Collection<Class> classes = FindActionUtil.allActionClasses()
      AmePluginUtil.showAme( "Classes found" )
      Collections.sort( classes as List, new Comparator<Class>() {
        @Override
        public int compare(final Class c1, final Class c2) {
          return c1.toString(  ).compareToIgnoreCase(c2.toString(  ));
        }
      } )
      AmePluginUtil.showAme( "Classes sorted" )
      consoleActByCl = new AmeConsole("", "actionIdByClass", project)
      for( aClass in classes ){
        consoleActByCl.print( "${cIdx++}. ${aClass.toString(  ).substring( 6 )}:" )
        consoleActByCl.print( "    ${aClass.name}" )
        consoleActByCl.print( "      ${aClass.getCanonicalName(  )}" )
        consoleActByCl.print( "        ${aClass.package.name}" )
        consoleActByCl.print( "  "
        // May be != if 'name' property is defined explicitly:
        // `com.vincenzodevivo.idea.deltautils.actions.BeautifyAction` vs `Beautify javascript`
          + (aClass.toString().substring(6)==aClass.name ? ' ' : 'Ц')
        // May be != as if `public static class Prev extends NextPrevParameterAction` (many):
        // `com.intellij.codeInsight.completion.NextPrevParameterAction$Prev` vs `... .Prev`;
        // also in liveplugin: `liveplugin.implementation.Actions$1` vs `null`
          + (aClass.toString().substring(6)==aClass.getCanonicalName(  ) ? ' ' : 'Ч')
        // Never:
          + (aClass.toString().substring(6).startsWith( aClass.package.name ) ? ' ' : 'Ш')
          + "     ${FindActionUtil.actionIdByClass(aClass)}" )
      }
      AmePluginUtil.showAme( "All printed, see consoles" )
/*
*/
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

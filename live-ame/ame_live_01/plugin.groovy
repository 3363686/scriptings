//import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.actionSystem.Shortcut
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

import static AmePluginUtil.*
import static FindActionUtil.actionById

import static liveplugin.PluginUtil.*
import static liveplugin.implementation.Actions.*

def comment = "CommentByLineComment"
def oldComment = "Old" + comment
String menuId = "my" + comment
String menuKey = "ctrl SLASH"

//KeymapManager.instance.activeKeymap.removeAllActionShortcuts(comment)
if( actionById(menuId) ){
  try{
    unregisterAction( menuId );
  }catch( Exception e ){
    LOG.error(e);
    show( "Error1 : $e" )
  }
  showAme( "$menuId unregistered" );
}
if( actionById(comment) ){
  try{
    registerAction( oldComment,"",null, oldComment, actionById(comment) );
  }catch( Exception e ){
    LOG.error(e);
    showAme( "Error1 : $e" )
  }
  try{
    unregisterAction(comment)
  }catch( Exception e ){
    LOG.error(e);
    show( "Error3 : $e" )
  }
  showAme( "$comment unregistered" );
}
registerAction( menuId, menuKey ){ AnActionEvent event ->
  runDocumentWriteAction(event.project) {
    if( !AmeComment.doComment(event) ){
      actionById(oldComment).actionPerformed(anActionEvent())
    }
  }
}
showAme( "$menuId registered" );

menuId = "ame live list 1"
menuKey = "alt BACK_QUOTE, 1"

registerAction( menuId, menuKey ){ AnActionEvent event ->
  def project = event.project
  def popupMenuContent = [
    "Sort Lines"    : {
      runDocumentWriteAction( project ){
        AmeSort.doSort(currentEditorIn( project ))
      }
    },
    "--"            : Separator.instance,
    "Project Files Stats": {
      def scope = GlobalSearchScope.projectScope(project)
      Map fileStats_2 = ([:])
      FileTypeManager.instance.registeredFileTypes.each {
        int fileCount = FileBasedIndex.instance.getContainingFiles(FileTypeIndex.NAME, it, scope).size()
        if (fileCount > 0) fileStats_2.put("'$it.defaultExtension' / '$it.name'", fileCount)
      }
      show("File count by type&name:<br/>" + fileStats_2.sort{ -it.value }.entrySet().join("<br/>"))

      Map fileList = ([:])
      FileTypeManager.instance.registeredFileTypes.each {
        def typeFiles= FileBasedIndex.instance.getContainingFiles(FileTypeIndex.NAME, it, scope)
        int fileCount = typeFiles.size()
        if (fileCount > 0) fileList.put("'$it.defaultExtension' / '$it.name'", [fileCount, "files:\n    " + typeFiles.join("\n    ")])
      }
      AmeConsole fileListConsole = new AmeConsole("", "Project files by type", project)
      fileListConsole.print("File count by type&name:\n" + fileList.sort{ -it.value[0] }.entrySet().join("\n"))
    },
    "Builtin Actions": {
      String oldPN = '', tstr = '', out, actId
      int pIdx = cIdx = aIdx = 0
      def actionManager = ActionManager.instance
      AnAction anAction
      Presentation pres
      AmeConsole consoleActByCl = new AmeConsole("", "Actions By Class", project)
      Collection<Class> classes = FindActionUtil.allActionClasses()
      Collections.sort( classes as List, new Comparator<Class>() {
        @Override
        public int compare(final Class c1, final Class c2) {
          return c1.toString(  ).compareToIgnoreCase(c2.toString(  ));
        }
      } )
      for( aClass in classes ){
        if( oldPN != aClass.package.name ){
          oldPN = aClass.package.name
          consoleActByCl.print( oldPN )
          pIdx++
        }
        actId = FindActionUtil.actionIdByClass(aClass)
        anAction = actionManager.getAction( actId )
        pres = anAction.getTemplatePresentation()
        tstr = ""
        anAction.shortcutSet.shortcuts.each{
          tstr += "|" + parseShortcut( it )
        }
        consoleActByCl.print( "  ${aClass.toString(  ).substring( 6 + oldPN.length(  )+1 )}:" + " ${pres.textWithMnemonic} [$tstr] ${pres.description} (id=$actId)" )
      }
      showAme( "All printed, see consoles" )
      // Convert package name based presentation of path to file path.
//      filePath = getFilePathInSvn() + getFilePathInProject()
//      AmePluginUtil.show_ame( filePath )

      try{
        BufferedWriter writer = null;
        try{
          writer = new BufferedWriter(new FileWriter(new File(moduleDirectory.getPath(), "WEB-INF/web.xml")));
//          writer.write(FileTemplateManager.getInstance().getJ2eeTemplate("web.2_3.xml").getText());
        }
        finally{
          if( writer != null ){
            writer.close();
          }
        }
      }
      catch( IOException e ){
        LOG.error(e);
      }
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
  ]
  showPopupMenu( popupMenuContent, menuId, event.dataContext )
}
showAme( "$menuId assigned to [$menuKey]." );

String parseShortcut( Shortcut sc ){
  String s = sc.toString(  )
  String t = ""
  int pos = 0
  String[][] trans = [
    ["ctrl ", "alt ", 'shift '],
    ['^',     '@',    '$']
  ]
  char[] mdf = new char[trans[0].length]
  while( (pos = s.indexOf( ']', pos+1 )) >=0 ){
    for( int i = 0; i < trans[0].length; i++ ){
      mdf[i] = (s.lastIndexOf( trans[0][i], pos ) >=0 ? trans[1][i] : '.' ) as char
    }
    t += ' ' + mdf.toString(  ) + ' ' + s.substring( s.lastIndexOf( ' ', pos )+1, pos )
  }
  return t
}

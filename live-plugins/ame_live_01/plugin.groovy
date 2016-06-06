/**
 * Live plugin # 01
 * Started by _ame_ in 2015.
 *
 * A. New or changed handlers (plugins) to some actions
 * B. New plugins inside the Menu that runs them
 */

// 0. Imports
  import com.intellij.openapi.actionSystem.ActionManager
  import com.intellij.openapi.actionSystem.AnAction
  import com.intellij.openapi.actionSystem.AnActionEvent
  import com.intellij.openapi.actionSystem.Presentation
  import com.intellij.openapi.actionSystem.Separator
  import com.intellij.openapi.actionSystem.Shortcut
  import com.intellij.openapi.fileTypes.FileTypeManager
  import com.intellij.openapi.keymap.KeymapManager
  import com.intellij.psi.search.FileTypeIndex
  import com.intellij.psi.search.GlobalSearchScope
  import com.intellij.util.indexing.FileBasedIndex

  import static AmePluginUtil.*
  import static FindActionUtil.actionById

  import static liveplugin.PluginUtil.*
  import static liveplugin.implementation.Actions.*

// if( isIdeStartup )  return   // Only run this plugin explicitly

// A.1. C++ and myCSV -style comments
  def comment = "CommentByLineComment"  // Org action id
  def oldComment = "Old" + comment      // Saved copy id
  String menuId = "my" + comment        // New handler id
  String menuKey = "ctrl SLASH"         // New handler shortcut

  // 1. Unregister new action if it was
    // unregisterAction( menuId ); // Excluded, because registerAction(...) does it

  /**
   * 2. We need to free menuKey from the original handler and attach new handler to it.
   * But we cannot simply unregister action, because we want be able to call it later.
   */
  KeymapManager.instance.activeKeymap.removeAllActionShortcuts(comment)

  // 3. New plugin to be executed on the old shortcut menuKey,
  //    but if it cannot process a file in the editor, old handler will be executed
  registerAction( menuId, menuKey ){ AnActionEvent event ->
    runDocumentWriteAction(event.project) {
      if( !AmeComment.doComment(event) ){
        actionById(oldComment).actionPerformed(anActionEvent()) // Why error!? _!ame_ 08.01.16
      }
    }
  }
  showAme( "$menuId registered" );

// B.1. Menu # 1
  menuId = "ame live list 1"
  menuKey = "alt BACK_QUOTE, 1"

  registerAction( menuId, menuKey ){ AnActionEvent event ->
    def project = event.project
    def popupMenuContent = [
// B.1.1. Sort Lines: AmeSort.doSort
      "Sort Lines"    : {
        runDocumentWriteAction( project ){
          AmeSort.doSort(currentEditorIn( project ))
        }
      },
      "--"            : Separator.instance,
// B.1.2. Project Files Stats: inline
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
// B.1.3. Builtin Actions: inline
      "Builtin Actions": {
        String oldPN = '', tstr = '', out, actId
        int pIdx = cIdx = aIdx = 0
        def actionManager = ActionManager.instance
        AnAction anAction
        Presentation pres
        AmeConsole consoleActByCl = new AmeConsole("", "Actions By Class", project)
        def prn = consoleActByCl.&print
        Collection<Class> classes = FindActionUtil.allActionClasses()
        Collections.sort( classes as List, new Comparator<Class>() {
          @Override
          public int compare(final Class c1, final Class c2) {
            return c1.toString(  ).compareToIgnoreCase(c2.toString(  ));
          }
        } )
        showAme( "All printed, see consoles" )
        try{
          BufferedWriter writer = null;
          // def wrt = writer.&write   // doesn't work here
          writer = new BufferedWriter(new FileWriter(new File(project.basePath,"testw.txt")));
          wrt = writer.&write   // here OK
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
            tstr = anAction.shortcutSet.shortcuts.each{ consoleActByCl.print(it); consoleActByCl.print(parseShortcut(it)); parseShortcut(it) }.join("& ")
            prn /*consoleActByCl.print*/( "  ${aClass.toString(  ).substring( 6 + oldPN.length(  )+1 )}:" + " ${pres.textWithMnemonic} [$tstr] ${pres.description} (id=$actId)" )
            wrt /*writer.write*/("  ${ aClass.toString().substring(6 + oldPN.length() + 1) }:" + " ${ pres.textWithMnemonic } [$tstr] ${ pres.description } (id=$actId)\n");
          }
          if( writer != null ){
            writer.close();
          }
        }
        catch( IOException e ){
          LOG.error(e);
        }
      },
// B.1.4. Tests and other garbage
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
  showAme( "$menuId assigned to  [$menuKey]. [v@08.01.16-12:27:37]" );

// 3. Subroutines
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
      t +=mdf.toString(  ) + ' ' + s.substring( s.lastIndexOf( ' ', pos )+1, pos )
    }
    return t
  }

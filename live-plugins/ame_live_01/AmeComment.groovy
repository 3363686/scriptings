/**
 * Add/Remove C++ and myCSV -style comments
 * Created by _ame_ on 21.11.2015 15:51.
 */
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import com.intellij.openapi.editor.actions.EditorActionUtil;

import org.apache.commons.lang.StringUtils;

import static AmePluginUtil.*
import static liveplugin.PluginUtil.currentEditorIn
import static liveplugin.PluginUtil.show

class AmeComment{
public static boolean doComment( AnActionEvent event ){
// 3 methods of access to current file to get its type
  def editor = currentEditorIn(event.project)
  // stackoverflow.com/questions/15000404/how-to-get-the-current-file-name-in-plugin-action-class
  VirtualFile vf1 = event.getData(PlatformDataKeys.VIRTUAL_FILE);
  // http://stackoverflow.com/questions/17915688/intellij-plugin-get-code-from-current-open-file
  VirtualFile vf2 = FileDocumentManager.getInstance().
   getFile(FileEditorManager.getInstance(event.project).selectedTextEditor.
    getDocument());
  //show("project: ${ event.project.toString() }")
  // stackoverflow.com/questions/29210411/get-current-active-file-full-address-in-intellij-idea
  PsiFile pf = PsiUtilBase.getPsiFileInEditor(editor, event.project);
  def ft = pf.fileType
// Set up on file type
  def cmt, keepFmt;
  switch( ft.defaultExtension ?: ft ){
    case "c":
    case "js":
    case "php":
    case "java":
    case "groovy":
      cmt = "//"
      keepFmt = false;
      break;
    case "csv":
      // show("file='$pf.virtualFile.canonicalPath' fileType='$ft' ('$ft.defaultExtension')")
      cmt = "#"
      keepFmt = true;
      break;
    default: return 0;    // Don't process unknown type, caller will pass event to IDEA
  }
// Prepare processing vars
  final Document doc = editor.getDocument();
  String str;
  int caretCol = editor.caretModel.logicalPosition.column;
  int startLine, endLine, line, lineBegin, lineEnd, cmtBegin;
  int addLen, cmtLen = cmt.length();
  boolean hasSelection = editor.getSelectionModel().hasSelection();
  boolean makeCmt = false;
// Lines range to comment / uncomment
  if( hasSelection ){
    startLine = doc.getLineNumber( editor.getSelectionModel().getSelectionStart() );
    endLine = doc.getLineNumber( editor.getSelectionModel().getSelectionEnd() );
    if( doc.getLineStartOffset( endLine ) == 0 ){
      endLine--;
    }
  }else{
    startLine = endLine = editor.caretModel.logicalPosition.line;
  }
// Make comment if any line in range is commented, otherwise uncomment
  for( line = startLine; line <= endLine; line++ ){
    if( ! doc.getCharsSequence().subSequence( doc.getLineStartOffset( line ), doc.getLineEndOffset( line )).toString().trim().startsWith(cmt) ){
      makeCmt = true;
      break;
    }
  }
// Do comment / uncomment
  if( makeCmt ){                        // Make comment
    if( keepFmt ){                          // Spec. processing for myCSV (overwrite ' ~' if it is)
      for( line = startLine; line <= endLine; line++ ){
        cmtBegin = EditorActionUtil.findFirstNonSpaceOffsetInRange(doc.getCharsSequence(), doc.getLineStartOffset(line), doc.getLineEndOffset(line));

        if( cmtBegin > caretCol || cmtBegin < 0 ){
          cmtBegin = caretCol;
        }
      }

      return 0;
    }else{                                  // Insert / append comment
      for( line = startLine; line <= endLine; line++ ){
        cmtBegin = EditorActionUtil.findFirstNonSpaceOffsetInRange(doc.getCharsSequence(), lineBegin = doc.getLineStartOffset(line), lineEnd = doc.getLineEndOffset(line));
        showAme("$caretCol, $cmtBegin, $lineBegin, $lineEnd");
        if( cmtBegin < 0 ){                                   // White / Empty line
          cmtBegin = caretCol+lineBegin;
        }
        if( (addLen = caretCol - (lineEnd - lineBegin)) >= 0 ){   // At or past EOL, append only
          doc.insertString( lineEnd, StringUtils.repeat( ' ', addLen ) + cmt + " " );
          editor.getCaretModel().moveCaretRelatively( addLen + cmtLen + 1, 0, false, false, false );
        }else{                                                    // Before EOL, insert and Down:
          if( cmtBegin > caretCol+lineBegin ){
            cmtBegin = caretCol+lineBegin;      // - if before FirstChar, then at cursor pos;
          }                                     // - if at/past FirstChar, then at FirstChar
          doc.insertString( cmtBegin, cmt + " " );    // Caret does not move(?)
          editor.getCaretModel().moveCaretRelatively( -(cmtLen + 1)*0, 1, false, false, false );
        }
      }
    }
    return 1;
  }else{                                // Uncomment
    for( line = startLine; line <= endLine; line++ ){
      str = doc.getCharsSequence().subSequence( lineBegin = doc.getLineStartOffset( line ), doc.getLineEndOffset( line )).toString();
      cmtBegin = str.indexOf(cmt);
      doc.deleteString( lineBegin+cmtBegin,
                        lineBegin+cmtBegin+cmtLen+(str.charAt(cmtBegin+cmtLen)==((char)' ')?1:0) )
      if( keepFmt ) doc.insertString( lineBegin+cmtBegin, " " );
    }
    return 1;
  }
}
}

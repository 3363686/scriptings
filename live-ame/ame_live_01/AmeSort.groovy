import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages

import static liveplugin.PluginUtil.show;

public class AmeSort{
  public static int ascending;
  public static int startCol;
  public static void doSort( Editor editor ){
    final Document doc = editor.getDocument();
    startCol = editor.caretModel.logicalPosition.column;
    int startLine;
    int endLine;

    boolean hasSelection = editor.getSelectionModel().hasSelection();
    if( hasSelection ){
      startLine = doc.getLineNumber( editor.getSelectionModel().getSelectionStart() );
      endLine = doc.getLineNumber( editor.getSelectionModel().getSelectionEnd() );
      if( doc.getLineStartOffset( endLine ) == editor.getSelectionModel().getSelectionEnd() ){
        endLine--;
      }
    }else{
      startLine = 0;
      endLine = doc.getLineCount() - 1;
    }

    def command = Messages.showInputDialogWithCheckBox( "Starting column:", "Column and direction", "Ascending",true,true,null,startCol.toString(),null)
    if( command.first == null ) return
    startCol = command.first.toInteger()
    if ( startCol < 0) return
    ascending = command.second ? 1 : -1
    show("Comparison started from column $startCol, ${command.second?"a":"de"}scending","Sorted")

    // Ignore last lines (usually one) which are only '\n'
    endLine = ignoreLastEmptyLines( doc, endLine );

    if( startLine >= endLine ){
      return;
    }

    // Extract text as a list of lines
    List<String> lines = extractLines( doc, startLine, endLine );

    // dumb sort
    SortUtils.defaultSort( lines );

    StringBuilder sortedText = joinLines( lines );

    // Remove last \n is sort has been applied on whole file and the file did not end with \n
    if( !hasSelection ){
      CharSequence charsSequence = doc.getCharsSequence();
      if( charsSequence.charAt( charsSequence.length() - 1 ) != '\n' ){
        sortedText.deleteCharAt( sortedText.length() - 1 );
      }
    }

    // Replace text
    int startOffset = doc.getLineStartOffset( startLine );
    int endOffset = doc.getLineEndOffset( endLine ) + doc.getLineSeparatorLength( endLine );

    editor.getDocument().replaceString( startOffset, endOffset, sortedText );
  }

  private static int ignoreLastEmptyLines( Document doc, int endLine ){
    while( endLine >= 0 ){
      if( doc.getLineEndOffset( endLine ) > doc.getLineStartOffset( endLine ) ){
        return endLine;
      }
      endLine--;
    }
    return -1;
  }

  private static List<String> extractLines( Document doc, int startLine, int endLine ){
    List<String> lines = new ArrayList<String>( endLine - startLine );
    for( int i = startLine; i <= endLine; i++ ){
      String line = extractLine( doc, i );
      lines.add( line );
    }
    return lines;
  }

  private static String extractLine( Document doc, int lineNumber ){
    int lineSeparatorLength = doc.getLineSeparatorLength( lineNumber );
    int startOffset = doc.getLineStartOffset( lineNumber );
    int endOffset = doc.getLineEndOffset( lineNumber ) + lineSeparatorLength;

    String line = doc.getCharsSequence().subSequence( startOffset, endOffset ).toString();

    // If last line has no \n, add it one
    // This causes adding a \n at the end of file when sort is applied on whole file and the file does not end
    // with \n... This is fixed after.
    if( lineSeparatorLength == 0 ){
      line += "\n";
    }

    return line;
  }

  private static StringBuilder joinLines( List<String> lines ){
    StringBuilder builder = new StringBuilder();
    for( String line : lines ){
      builder.append( line );
    }
    return builder;
  }
}

public class SortUtils{

  private static String substr ( String s ){
    return s.length(  ) < AmeSort.startCol ? "" : s.substring(AmeSort.startCol)
  }

  private static final Comparator<String> DEFAULT_COMPARATOR = new DefaultComparator();

  public static void defaultSort( List<String> lines ){
    Collections.sort( lines, DEFAULT_COMPARATOR );
  }

  private static class DefaultComparator implements Comparator<String>{
    public int compare( String s1, String s2 ){
//      return AmeSort.ascending * s1.substring(AmeSort.startCol*0).compareToIgnoreCase( s2.substring(AmeSort.startCol*0) );
      return AmeSort.ascending * substr(s1).compareToIgnoreCase( substr(s2) );
    }
  }
}

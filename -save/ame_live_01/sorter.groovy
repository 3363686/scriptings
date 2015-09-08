import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction

import static liveplugin.PluginUtil.show;

public class SortAction
/*extends TextComponentEditorAction*/
{
  static saMarker = "SortAction attached"
  public sapMarker() { "SortAction public attached"}

  // private static class Handler extends EditorWriteActionHandler{
  public void doSort( Editor editor ){
    final Document doc = editor.getDocument();
    int shi = 0;
    show( "doSort ${ ++shi }" )

    int startLine;
    int endLine;
    show( "doSort ${ ++shi }" )

    boolean hasSelection = editor.getSelectionModel().hasSelection();
    show( "doSort ${ ++shi }" )
    if( hasSelection ){
      show( "doSort ${ ++shi }" )
      startLine = doc.getLineNumber( editor.getSelectionModel().getSelectionStart() );
      endLine = doc.getLineNumber( editor.getSelectionModel().getSelectionEnd() );
      if( doc.getLineStartOffset( endLine ) == editor.getSelectionModel().getSelectionEnd() ){
        endLine--;
      }
      show( "doSort ${ shi = 8 }" )
    }else{
      show( "doSort ${ ++shi }" )
      startLine = 0;
      endLine = doc.getLineCount() - 1;
      show( "doSort ${ shi = 9 }" )
    }

    // Ignore last lines (usually one) which are only '\n'
    show( "doSort ${ shi = 10 }" )
    endLine = ignoreLastEmptyLines( doc, endLine );

    show( "startLine $startLine >= endLine $endLine" )
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

  private int ignoreLastEmptyLines( Document doc, int endLine ){
    show( "doSort ${ shi = 20 }" )
    while( endLine >= 0 ){
      if( doc.getLineEndOffset( endLine ) > doc.getLineStartOffset( endLine ) ){
        show( "doSort ${ shi = 21 }" )
        return endLine;
      }
      endLine--;
    }
    show( "doSort ${ shi = 22 }" )
    return -1;
  }

  private List<String> extractLines( Document doc, int startLine, int endLine ){
    List<String> lines = new ArrayList<String>( endLine - startLine );
    for( int i = startLine; i <= endLine; i++ ){
      String line = extractLine( doc, i );
      lines.add( line );
    }
    return lines;
  }

  private String extractLine( Document doc, int lineNumber ){
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

  private StringBuilder joinLines( List<String> lines ){
    StringBuilder builder = new StringBuilder();
    for( String line : lines ){
      builder.append( line );
    }
    return builder;
  }
  //}
}

public class SortUtils{

  static suMarker = "SortUtils attached"

  private static
  final Comparator<String> DEFAULT_COMPARATOR = new DefaultComparator();

  public static void defaultSort( List<String> lines ){
    Collections.sort( lines, DEFAULT_COMPARATOR );
  }

  private static class DefaultComparator implements Comparator<String>{
    public int compare( String s1, String s2 ){
      return s1.compareToIgnoreCase( s2 );
    }
  }
}

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex

import static liveplugin.PluginUtil.show

// import AmeConsole.*

def fileStats = FileTypeManager.instance.registeredFileTypes.inject([:]) { Map stats, FileType fileType ->
  def scope = GlobalSearchScope.projectScope(project)
  int fileCount = FileBasedIndex.instance.getContainingFiles(FileTypeIndex.NAME, fileType, scope).size()
  if (fileCount > 0) stats.put("'$fileType.defaultExtension'", fileCount)
  stats
}.sort{ -it.value }
show("File count by type:<br/>" + fileStats.entrySet().join("<br/>"))

def scope = GlobalSearchScope.projectScope(project)
Map fileStats_2 = ([:])
FileTypeManager.instance.registeredFileTypes.each {
  int fileCount = FileBasedIndex.instance.getContainingFiles(FileTypeIndex.NAME, it, scope).size()
  if (fileCount > 0) fileStats_2.put("'$it.defaultExtension' / '$it.name'", fileCount)
}
show("File count by type&name:<br/>" + fileStats_2.sort{ -it.value }.entrySet().join("<br/>"))

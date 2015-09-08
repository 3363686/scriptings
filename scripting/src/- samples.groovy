write { "PsiManager.java".firstFile().document().setText("Rollback!") }

pool { Thread.sleep(5000); IDE.print("123") }

action("TryMe!", "alt shift P") { e->
  e = PSI_ELEMENT.from(e)
  res = e != null &&  finds.canFindUsages(e)
  IDE.print("can find usages of '" + e +"'? " + (res ? "Yes!" : "Nope"))
}

timer("bump", 500) {
  IDE.print(System.currentTimeMillis() + ": bump!")
}
dispose("bump")

def ep = "com.intellij.iconLayerProvider".ep()
ep.registerExtension(new com.intellij.ide.IconLayerProvider() {
  String getLayerDescription() { return "123" }

  javax.swing.Icon getLayerIcon(com.intellij.openapi.util.Iconable element, boolean isLocked) {
    "config".equals(element.getName()) ? com.intellij.icons.AllIcons.Nodes.FinalMark : null
  }
})
ep.unregisterExtension(ep.getExtensions().last())


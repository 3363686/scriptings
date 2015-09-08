/**
 * Created by _ame_ on 30.06.2015 11:40.
 * Script Monkey file!
 */
echo("This is hello from test.js");

var antPkgs = new JavaImporter(org.apache.tools.ant);

with (antPkgs) {

  function showAntVersion() {
    echo(Main.getAntVersion());
  }

}
var tools = showAntVersion();
echo(tools);
//alert(tools);

var pkgs = new JavaImporter(com.intellij.openapi.application, com.intellij.ide.plugins, com.intellij.openapi.extensions, com.intellij.openapi.actionSystem, com.boxysystems.scriptmonkey.intellij);

with (pkgs){
  //var plugins1 = intellij.application.getPlugins();
  //var plugins1 = intellij.application.getPlugins();
  //echo( plugins1[1].name );
  echo( "plugins1[1].name" );
}

with (pkgs){
  function my_listPlugins(){
    var plugins = intellij.application.getPlugins();
    for( var i = 0; i < plugins.length; i++ ){
      echo( "Name = " + plugins[i].name + ", Vendor = " + plugins[i].vendor + ", Version = " + plugins[i].version );
    }
  }
}

function my_listPkgs(){
  //var plugins = intellij.application.getPlugins();
  var plugins = intellij.application;
  for( var i = 0; i < plugins.length; i++ ){
    echo( "Name = " + plugins[i].name + ", Vendor = " + plugins[i].vendor + ", Version = " + plugins[i].version );
  }
}
my_listPkgs();

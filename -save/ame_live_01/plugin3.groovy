import util.*

import static liveplugin.PluginUtil.*

// For more util methods see
// https://github.com/dkandalov/live-plugin/blob/master/src_groovy/liveplugin/PluginUtil.groovy

// log message in "Event Log"
log("Hello IntelliJ")

// popup notification
show("IntelliJ", "Hello 0")

// using imported class
show(AClass.sayHello(), "Hello 1")
show(BClass.sayHello(), "Hello 2")

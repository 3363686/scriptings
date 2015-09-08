import static liveplugin.PluginUtil.*
import util.*
import util2.*

public class Data03{
  static t = "Data03 text"
  public p = "Data03 pext"
  String n = "Data03 next"
  static f(){ "Data03 fext" }
}

class Data04{
  static t = "Data04 text"
  public p = "Data04 pext"
  String n = "Data04 next"
  static f(){ "Data04 fext" }
}

// using imported class
show(AClass.sayHello(), "Hello 1")
show(BClass.sayHello(), "Hello 2")
show(AClass_.sayHello(), "Hello 1")
show(BClass_.sayHello(), "Hello 2")

// Testing:
//show( Data02.t )
show( Data02.f() )
show( Data02_.f() )
//d1 = new Data01()
//show( d1.n )
//show( d1.p )

// Err;
//show( testData.Data01.t )
//show( testData.Data01.p )
//show( testData.Data01.n )
//show( testData.Data01.f() )

// OK:
//show( Data03.t )
//show( Data03.f() )
//d3 = new Data03()
//show( d3.n )
//show( d3.p )
//show( Data04.t )
//show( Data04.f() )
//d4 = new Data04()
//show( d4.n )
//show( d4.p )

// OK and can rename:

// OK and can't rename class:


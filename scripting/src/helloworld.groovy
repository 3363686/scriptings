
// https://ru.wikibooks.org/wiki/Groovy
println "Hello World!"

abstract class Shape {
    final name
    Shape(name) { this.name = name }
    abstract printName()
}

class Circle extends Shape {
    final radius
    Circle(radius) {
        super('circle')
        this.radius = radius
    }
    def area() { Math.PI * radius * radius }
    def printName() {
        print "I am a $name. "
    }
}

class Rectangle extends Shape {
    final length, breadth
    def Rectangle(length, breadth) {
        super("rectangle")
        this.length = length
        this.breadth = breadth
    }
    def area() { length * breadth }
    def printName() {
        print "I am a $name. "
    }
}
shapes = [new Circle(4.2), new Rectangle(5, 7)]
shapes.each {
    shape -> shape.printName()
    println("area = " + shape.area())
}

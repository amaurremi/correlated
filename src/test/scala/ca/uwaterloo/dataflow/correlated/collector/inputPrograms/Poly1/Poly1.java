package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly1;

public class Poly1 {

    public static void main(String[] args) {
        PolyParent p = args.length > 0 ? new PolyChild1() : new PolyChild2();
        p.foo();    // not polymorphic
        p.bar();    // not polymorphic
    }
}

class PolyParent {

    void foo() {}

    void bar() {}
}

class PolyChild1 extends PolyParent {}

class PolyChild2 extends PolyParent {}

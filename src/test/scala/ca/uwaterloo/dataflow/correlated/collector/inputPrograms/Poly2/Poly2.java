package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly2;

public class Poly2 {

    public static void main(String[] args) {
        PolyParent p = args.length > 0 ? new PolyChild1() : new PolyChild2();
        p.foo();    // polymorphic
        p.bar();    // not polymorphic
    }
}

abstract class PolyParent {

    void foo() {}

    void bar() {}
}

class PolyChild1 extends PolyParent {

    void foo() {}
}

class PolyChild2 extends PolyParent {

    void foo() {}
}
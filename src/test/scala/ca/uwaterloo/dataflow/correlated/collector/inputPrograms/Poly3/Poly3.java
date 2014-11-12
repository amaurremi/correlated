package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly3;

public class Poly3 {

    public static void main(String[] args) {
        boolean b = args.length > 0;
        PolyParent p = b ? new PolyChild1() : new PolyChild2();
        p.foo(b);    // polymorphic
    }
}

abstract class PolyParent {

    void foo(boolean b) {}
}

class PolyChild1 extends PolyParent {

    void foo(boolean b) {
        PolyParent p = b ? new PolyChild1() : new PolyChild2();
        p.foo(b);    // polymorphic
    }
}

class PolyChild2 extends PolyParent {

    void foo(boolean b) {}
}
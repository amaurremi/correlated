package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly4;

public class Poly4 {

    public static void main(String[] args) {
        boolean b = args.length > 0;
        boolean b2 = args.length > 1;
        PolyParent p = b ? new PolyChild1(b2, b2) : new PolyChild2();
        p.foo(b, b2);    // polymorphic
    }
}

abstract class PolyParent {

    void foo(boolean b, boolean b2) {}
}

class PolyChild1 extends PolyParent {

    PolyChild1(boolean b, boolean b2) {
        PolyParent p = b ? new PolyChild1(b2, b2) : new PolyChild2();
        p.foo(b, b2);    // polymorphic
    }

    void foo(boolean b, boolean b2) {
        PolyParent p = b ? new PolyChild1(b2, b2) : new PolyChild2();
        p.foo(b, b2);    // polymorphic
    }
}

class PolyChild2 extends PolyParent {

    void foo(boolean b, boolean b2) {}
}
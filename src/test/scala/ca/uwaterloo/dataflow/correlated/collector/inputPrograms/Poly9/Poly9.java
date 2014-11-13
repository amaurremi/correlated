package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly9;

public class Poly9 {

    public static void main(String[] args) {
        PolyParent p1 = new PolyChild1();
        PolyParent p2 = new PolyChild2();
        p2.foo();
        bar(p1);
    }

    static void bar(PolyParent p) {
        p.foo(); // not polymorphic
    }
}

abstract class PolyParent {

    void foo() {}
}

class PolyChild1 extends PolyParent {

    void foo() {}
}

class PolyChild2 extends PolyParent {

    void foo() {}
}

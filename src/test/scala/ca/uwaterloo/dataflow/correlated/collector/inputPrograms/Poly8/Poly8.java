package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly8;

public class Poly8 {

    public static void main(String[] args) {
        PolyParent p1 = new PolyChild1();
        bar(p1);
        PolyParent p2 = new PolyChild2();
        bar(p2);
    }

    static void bar(PolyParent p) {
        p.foo(); // polymorphic
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
package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly2;

public class Poly2 {

    public static void main(String[] args) {
        PolyParent p = args.length > 0 ? new PolyChild1() : new PolyChild2();
        p.foo();    // polymorphic
        p.bar();    // not polymorphic
    }
}

class PolyParent {

    void foo() {}

    void bar() {}
}

class PolyChild1 extends PolyParent {

    void foo() {
        System.out.println("1");
    }
}

class PolyChild2 extends PolyParent {

    void foo() {
        System.out.println("2");
    }
}
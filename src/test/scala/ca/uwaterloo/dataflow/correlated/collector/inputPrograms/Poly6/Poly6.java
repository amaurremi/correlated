package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly6;

public class Poly6 {

    public static void main(String[] args) {
        int length = args.length;
        boolean b = length > 0;
        PolyParent p = b ? new PolyChild1() : new PolyChild2();
        p.foo(b);    // polymorphic
        if (length > 1) {
            p.foo(b);    // polymorphic
        } else {
            p.foo(!b);   // polymorphic
        }
        p.foo(b);        // polymorphic
        for (int i = 0; i < length && p.foo(b) < length /* polymorphic */; i++) {
            p.foo(b);    // polymorphic
        }
    }
}

abstract class PolyParent {

    int foo(boolean b) {
        return 1;
    }
}

class PolyChild1 extends PolyParent {

    int foo(boolean b) {
        return 2;
    }
}

class PolyChild2 extends PolyParent {

    int foo(boolean b) {
        return 3;
    }
}
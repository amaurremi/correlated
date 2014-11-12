package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.Poly7;

public class Poly7 {

    public static void main(String[] args) {
        int length = args.length;
        boolean b = length > 0;
        PolyParent p = b ? new PolyChild1() : new PolyChild2();
        p.foo(length);    // polymorphic 1
    }
}

abstract class PolyParent {

    void foo(int i) {}
}

class PolyChild1 extends PolyParent {

    void foo(int i) {
        PolyParent p = i > 0 ? new PolyChild1() : new PolyChild2();
        if (i > 0) {
            p.foo(i);    // polymorphic 2
            if (i > 1) {
                p.foo(i);    // polymorphic 3
                if (i > 2) {
                    p.foo(i);    // polymorphic 4
                    if (i > 3) {
                        p.foo(i);    // polymorphic 5
                        if (i > 4) {
                            p.foo(i);    // polymorphic 6
                            while(i > 5) {
                                p.foo(i++);    // polymorphic 7
                            }
                        }
                    }
                }
            }
        }
    }
}

class PolyChild2 extends PolyParent {

    void foo(int i) {}
}
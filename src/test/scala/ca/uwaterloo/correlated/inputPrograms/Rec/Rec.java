package ca.uwaterloo.correlated.inputPrograms.Rec;

public class Rec {
    public static void main(String[] args) {
        new Rec().foo(args[0]);
    }

    // foo and bar are mutually recursive
    void foo(String arg) {
        bar(arg);
    }

    // bar contains two CCs
    void bar(String arg) {
        Rec rec;
        if (arg.isEmpty()) {
            rec = new Rec();
        } else {
            rec = new A();
        }
        rec.foo(arg);
        rec.bar(arg);
        rec.baz();
    }

    // baz is recursive
    void baz() {
        baz();
    }
}

class A extends Rec {

    @Override
    void foo(String arg) {}
    @Override
    void bar(String arg) {}
}

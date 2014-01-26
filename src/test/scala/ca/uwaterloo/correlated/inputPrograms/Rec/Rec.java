package Rec;

public class Rec {
    public static void main(String[] args) {
        new Rec().foo();
    }

    // foo and bar are mutually recursive
    void foo() {
        bar();
    }

    // bar contains two CCs
    void bar() {
        foo();
        baz();
    }

    // baz is recursive
    void baz() {
        baz();
    }
}

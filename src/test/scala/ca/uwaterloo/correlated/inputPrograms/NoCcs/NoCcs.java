package NoCcs;

public class NoCcs {

    public static void main(String[] args) {
        NoCcs a = new NoCcs();
        // Two invocations on receiver a, but one of them is private
        a.foo();
        a.bar();
    }

    private void foo() {}

    void bar() {
        // static method invocations are not counted as correlated calls
        main(null);
        main(null);
        NoCcs noCcs = new NoCcs();
        // Monomorphic correlated calls are not counted
        noCcs.bar();
        noCcs.foo();
        // Monomorphic correlated calls are not counted
        foo();
        foo();
    }

    // should not be count since baz is unreachable
    void baz() {
        bar();
        bar();
    }
}

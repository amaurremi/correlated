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
    }
}

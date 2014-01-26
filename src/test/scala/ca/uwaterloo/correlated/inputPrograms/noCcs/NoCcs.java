package NoCcs;

public class NoCcs {

    public static void main(String[] args) {
        NoCcs a = new NoCcs();
        // Two invocations on method a, one of which is private
        a.foo();
        a.bar();
    }

    private void foo() {}

    void bar() {}
}

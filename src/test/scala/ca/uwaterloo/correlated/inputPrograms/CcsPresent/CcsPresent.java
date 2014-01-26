package CcsPresent;

public class CcsPresent {

    public static void main(String[] args) {
        CcsPresent cp1 = new CcsPresent();
        // first correlated call
        cp1.foo();
        cp1.foo();
        CcsPresent cp2 = new CcsPresent();
        // second correlated call
        cp2.foo();
        cp2.bar();
    }

    void foo() {
        // third correlated call
        bar();
        bar();
    }

    void bar() {}
}

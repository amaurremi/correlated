package ca.uwaterloo.correlated.inputPrograms.NoCcs;

// This class does not have correlated calls
public class NoCcs {

    public static void main(String[] args) {
        NoCcs a = new NoCcs();
        NoCcs b = new NoCcs();

        // Invoking the same method on two different objects of the same class
        a.bar();
        b.bar();
          //todo remove
        a.bar();

        // Invoking a second call on a, but on a private method
        a.foo();
    }

    private void foo() {
        System.out.println("foo");
    }

    void bar() {
        System.out.println("bar");
    }
}

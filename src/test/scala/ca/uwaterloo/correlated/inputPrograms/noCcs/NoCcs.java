package ca.uwaterloo.correlated.inputPrograms.NoCcs;

// This class does not have correlated calls
public class NoCcs {

    public static void main(String[] args) {
        NoCcs a = new NoCcs();
        NoCcs b = new NoCcs();

        // Invoking the same method on two different objects of the same class
        String aStr = a.toString();
        String bStr = b.toString();

        // Invoking a second call on a, but on a private method
        int c = a.foo();
    }

    private int foo() {
        return 2;
    }
}

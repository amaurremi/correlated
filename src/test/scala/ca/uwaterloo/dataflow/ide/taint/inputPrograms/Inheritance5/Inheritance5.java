package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

abstract class X {
    public abstract String foo(); 
}

class Y extends X {
    public String foo(){ return Inheritance5.secret(); }
}

public class Inheritance5 {

    public static void main(String[] args) {
        X x = new Y();
        SecretAssertions.shouldBeSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

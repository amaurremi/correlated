package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

abstract class X {
    public abstract String foo();
}

class Y extends X {
    public String foo(){ return "not secret"; }
}

public class Inheritance6 {

    public static void main(String[] args) {
        X x = new Y();
        SecretAssertions.notSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

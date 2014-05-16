package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String foo(){ return "not secret"; }
}

class Y extends X {
    public String foo(){ return "not secret"; }
}

public class Inheritance2 {

    public static void main(String[] args) {
        X x = new Y();
        SecretAssertions.notSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

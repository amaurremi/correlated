package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance12;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String foo(String s) { return "not secret"; }
}

class Y extends X {
    public String foo(String s){ return s; }
}

public class Inheritance12 {

    public static void main(String[] args) {
        X x = args.length == 0 ? new Y() : new X();
        SecretAssertions.shouldBeSecret(x.foo(secret()));
    }

    static String secret() {
        return "secret";
    }
}

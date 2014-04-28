package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String foo(){ return Inheritance.secret(); }
}

class Y extends X {
    public String foo(){ return Inheritance.secret(); }
}

public class Inheritance {

    public static void main(String[] args) {
        X x = new Y();
        SecretAssertions.shouldBeSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

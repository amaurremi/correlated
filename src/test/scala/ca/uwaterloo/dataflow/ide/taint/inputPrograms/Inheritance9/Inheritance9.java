package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance9;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String foo(){ return "not secret"; }
}

class Y extends X {
}

class Z extends Y {
    public String foo(){ return Inheritance9.secret(); }
}

public class Inheritance9 {

    public static void main(String[] args) {
        X x = new Z();
        SecretAssertions.shouldBeSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

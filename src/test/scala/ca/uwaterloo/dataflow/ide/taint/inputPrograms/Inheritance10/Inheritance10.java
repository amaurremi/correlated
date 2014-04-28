package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance10;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

interface X {
    public String foo();
}

class Y {
    public String foo(){ return Inheritance10.secret(); }
}

class Z extends Y implements X { }

public class Inheritance10 {

    public static void main(String[] args) {
        X x = new Z();
        SecretAssertions.shouldBeSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

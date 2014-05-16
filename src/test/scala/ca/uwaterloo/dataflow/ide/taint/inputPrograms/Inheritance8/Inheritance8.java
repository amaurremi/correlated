package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance8;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

interface X {
    public String foo();
}

class Y implements X {
    public String foo(){ return "not secret"; }
}

public class Inheritance8 {

    public static void main(String[] args) {
        X x = new Y();
        SecretAssertions.notSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

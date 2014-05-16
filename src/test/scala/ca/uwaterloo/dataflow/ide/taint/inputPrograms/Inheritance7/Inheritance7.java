package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

interface X {
    public String foo(); 
}

class Y implements X {
    public String foo(){ return Inheritance7.secret(); }
}

public class Inheritance7 {

    public static void main(String[] args) {
        X x = new Y();
        SecretAssertions.secret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

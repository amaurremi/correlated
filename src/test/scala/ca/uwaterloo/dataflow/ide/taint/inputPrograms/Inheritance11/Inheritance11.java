package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance11;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String foo(){ return "not secret"; }
}

public class Inheritance11 {

    public static void main(String[] args) {
        X x = new X(){
          public String foo(){ return secret(); }
        };
        SecretAssertions.secret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

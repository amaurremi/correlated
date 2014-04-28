package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String foo(){ return "not secret"; }
}

class Y extends X {
    public String foo(){ return Inheritance4.secret(); }
}

public class Inheritance4 {

    public static void main(String[] args) {
        X x = null;
        if (args.length == 3){
          x = new X();
        } else {
          x = new Y();
        }
        SecretAssertions.shouldBeSecret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

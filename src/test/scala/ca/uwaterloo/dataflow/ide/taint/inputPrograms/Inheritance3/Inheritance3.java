package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Inheritance3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String foo(){ return Inheritance3.secret(); }
}

class Y extends X {
    public String foo(){ return "not secret"; }
}

public class Inheritance3 {

    public static void main(String[] args) {
        X x = null;
        if (args.length == 3){
          x = new X();
        } else {
          x = new Y();
        }
        SecretAssertions.secret(x.foo());
    }

    static String secret() {
        return "secret";
    }
}

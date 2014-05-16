package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    String f;
}

public class Field5 {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.secret(s);
        X x = new X();
        x.f = s;
        X x2 = new X();
        x2.f = "not secret";
        SecretAssertions.secret(x.f);
        SecretAssertions.secret(x2.f); // treat instance fields as globals
    }

    static String secret() {
        return "secret";
    }
}

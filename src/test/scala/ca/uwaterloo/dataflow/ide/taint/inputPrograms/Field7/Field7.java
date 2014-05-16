package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    String f;
    String g;
}

public class Field7 {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.secret(s);
        X x1 = new X();
        X x2 = new X();
        x1.f = s;
        x2.g = "not secret";
        SecretAssertions.secret(x1.f);
        SecretAssertions.notSecret(x2.g); // field-sensitive
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    String f;
}

public class Field4 {

    public static void main(String[] args) {
        String s = "not secret";
        SecretAssertions.notSecret(s);
        X x = new X();
        x.f = s;
        SecretAssertions.notSecret(x.f);
    }

    static String secret() {
        return "secret";
    }
}

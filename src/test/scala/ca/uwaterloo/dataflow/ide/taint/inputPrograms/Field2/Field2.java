package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    static String f;
}

public class Field2 {

    public static void main(String[] args) {
        String s = "not secret";
        SecretAssertions.shouldNotBeSecret(s);
        X.f = s;
        SecretAssertions.shouldNotBeSecret(X.f);
    }

    static String secret() {
        return "secret";
    }
}

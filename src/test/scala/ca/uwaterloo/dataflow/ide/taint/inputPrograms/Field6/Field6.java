package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    static String f;
    static String g;
}

public class Field6 {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.secret(s);
        X.f = s;
        X.g = "not secret";
        SecretAssertions.secret(X.f);
        SecretAssertions.notSecret(X.g); // field-sensitive
    }

    static String secret() {
        return "secret";
    }
}

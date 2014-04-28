package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    static String f;
}

public class Field {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.shouldBeSecret(s);
        X.f = s;
        SecretAssertions.shouldBeSecret(X.f);
    }

    static String secret() {
        return "secret";
    }
}

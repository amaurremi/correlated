package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FieldAsParameter;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    static String f;

    X(String s) {
        f = s;
    }

    String bar() {
        return f;
    }
}

public class FieldAsParameter {

    static String fieldString;

    public static void main(String[] args) {
        fieldString = secret();
        f(fieldString);
        SecretAssertions.secret(new X(secret()).bar());
    }

    static String secret() {
        return "secret";
    }

    static void f(String string) {
        SecretAssertions.secret(string);
    }
}

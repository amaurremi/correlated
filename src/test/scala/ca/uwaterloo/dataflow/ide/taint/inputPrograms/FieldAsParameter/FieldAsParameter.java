package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FieldAsParameter;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    static String f;
}

public class FieldAsParameter {

    static String fieldString;

    public static void main(String[] args) {
        fieldString = secret();
        f(fieldString);
    }

    static String secret() {
        return "secret";
    }

    static void f(String string) {
        SecretAssertions.shouldBeSecret(string);
    }
}

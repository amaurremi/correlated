package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field8;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Field8 {

    static String s = secret();

    public static void main(String[] args) {
        SecretAssertions.shouldBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

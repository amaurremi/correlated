package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field9;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Field9 {

    String s = secret();

    public static void main(String[] args) {
        SecretAssertions.shouldBeSecret(new Field9().s);
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Array {

    public static void main(String[] args) {
        String s = secret();
        String[] strings = new String[1];
        strings[0] = s;
        SecretAssertions.secret(strings[0]);
    }

    static String secret() {
        return "secret";
    }
}

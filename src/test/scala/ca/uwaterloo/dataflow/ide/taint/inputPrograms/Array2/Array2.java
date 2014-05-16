package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Array2 {

    public static void main(String[] args) {
        String s = "not secret";
        String[] strings = new String[1];
        strings[0] = s;
        SecretAssertions.notSecret(strings[0]);
    }

    static String secret() {
        return "secret";
    }
}

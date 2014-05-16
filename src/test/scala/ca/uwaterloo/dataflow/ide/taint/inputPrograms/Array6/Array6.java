package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Array6 {

    public static void main(String[] args) {
        String[] strings = new String[1];
        strings[0] = secret();
        SecretAssertions.secret(strings[0]); // all arrays should be from now on secret
        strings[1] = "not secret";
        SecretAssertions.secret(strings[1]);
        String[] strings2 = new String[1];
        strings[0] = "not secret";
        SecretAssertions.secret(strings[0]);
    }

    static String secret() {
        return "secret";
    }
}

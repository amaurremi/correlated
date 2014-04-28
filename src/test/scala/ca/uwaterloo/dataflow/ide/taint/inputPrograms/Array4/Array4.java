package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// multidimensional array
public class Array4 {

    public static void main(String[] args) {
        String s = "not secret";
        String[][] strings = new String[][]{ { s } };
        SecretAssertions.shouldNotBeSecret(strings[0][0]);
    }

    static String secret() {
        return "secret";
    }
}

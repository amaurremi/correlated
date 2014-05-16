package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// multidimensional array
public class Array3 {

    public static void main(String[] args) {
        String s = secret();
        String[][] strings = new String[][]{ { s } };
        SecretAssertions.secret(strings[0][0]);
    }

    static String secret() {
        return "secret";
    }
}

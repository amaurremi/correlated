package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Array7 {

    public static void main(String[] args) {
        String[] strings = new String[1];
        strings[0] = secret();
        SecretAssertions.shouldBeSecret(strings[0]); // all arrays should be from now on secret
        Integer[] integers = new Integer[1];
        integers[0] = 1;
        SecretAssertions.shouldNotBeSecret(integers[0]);
    }

    static String secret() {
        return "secret";
    }
}

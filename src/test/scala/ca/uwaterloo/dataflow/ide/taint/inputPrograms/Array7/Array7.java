package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Array7 {

    public static void main(String[] args) {
        String[] strings = new String[1];
        strings[0] = secret();
        SecretAssertions.secret(strings[0]); // all arrays should be from now on secret
        Integer[] integers = new Integer[1];
        integers[0] = 1;
        SecretAssertions.notSecret(integers[0]);
        Object[] objects = new Object[1];
        objects[0] = new Object();
        SecretAssertions.secret(objects[0]);
        char[] chars = new char[1];
        chars[0] = 'n';
        SecretAssertions.notSecret(chars[0]);
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Array5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Array5 {

    public static void main(String[] args) {
        String s1 = secret();
        String s2 = "not secret";
        String[] strings = new String[]{ s1, s2 };
        for (int i=0; i < strings.length; i++){
          SecretAssertions.shouldBeSecret(strings[i]);
        }
    }

    static String secret() {
        return "secret";
    }
}

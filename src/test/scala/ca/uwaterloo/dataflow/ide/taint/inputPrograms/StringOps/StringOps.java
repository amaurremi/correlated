package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringOps;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringOps {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.shouldBeSecret(s);
        String s2 = s.toLowerCase();
        String s3 = s.substring(0);
        String s4 = s.trim();
        String s5 = s.toUpperCase();
        SecretAssertions.shouldBeSecret(s2);
        SecretAssertions.shouldBeSecret(s3);
        SecretAssertions.shouldBeSecret(s4);
        SecretAssertions.shouldBeSecret(s5);
    }

    static String secret() {
        return "secret";
    }
}

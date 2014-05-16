package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringOps;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringOps {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.secret(s);
        String s2 = "".trim();
        SecretAssertions.notSecret(s2);
        String s3 = s.substring(0);
        String s4 = s.trim();
        String s5 = s.toUpperCase();
        SecretAssertions.secret(s3);
        SecretAssertions.secret(s4);
        SecretAssertions.secret(s5);
    }

    static String secret() {
        return "secret";
    }
}

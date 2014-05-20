package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder {

    public static void main(String[] args) {
       String s1 = secret();
       java.lang.StringBuilder sb = new java.lang.StringBuilder(s1);
       String s3 = sb.toString();
       SecretAssertions.secret(s3);

        java.lang.StringBuilder sb2 = new java.lang.StringBuilder("not secret");
        String s4 = sb2.toString();
        SecretAssertions.notSecret(s4);

    }

    static String f() {
        String s = secret();
        return s;
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder6 {

    public static void main(String[] args) {
       String s = secret();
       String s2 = s + "not secret";
       SecretAssertions.secret(s2);
       String s3 = "not secret" + s;
       SecretAssertions.secret(s3);
       String s4 = "not secret" + "not secret";
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

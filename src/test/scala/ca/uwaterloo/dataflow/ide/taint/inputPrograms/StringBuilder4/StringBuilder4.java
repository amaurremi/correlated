package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder4 {

    public static void main(String[] args) {
       String s1 = "not secret";
       StringBuilder sb1 = new StringBuilder(s1);
       String s2 = secret();
       sb1.append(s2, 0, 1);
       String s3 = sb1.toString();
       SecretAssertions.secret(s3);
    }

    static String f() {
        String s = secret();
        return s;
    }

    static String secret() {
        return "secret";
    }
}

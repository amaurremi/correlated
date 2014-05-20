package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder3 {

    public static void main(String[] args) {
       String s1 = "not secret";
       StringBuilder sb1 = new StringBuilder(s1);
       String s2 = secret();
       StringBuilder sb2 = new StringBuilder(s2);
       sb1.append(sb2);
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

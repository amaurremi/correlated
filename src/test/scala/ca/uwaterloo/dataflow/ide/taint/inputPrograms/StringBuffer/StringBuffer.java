package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuffer;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuffer {

    public static void main(String[] args) {
       String s1 = secret();
       java.lang.StringBuffer sb = new java.lang.StringBuffer(s1);
       String s3 = sb.toString();
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

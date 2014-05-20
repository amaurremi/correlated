package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuffer5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuffer5 {

    public static void main(String[] args) {
       String s1 = "not secret";
       StringBuffer sb1 = new StringBuffer(s1);
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

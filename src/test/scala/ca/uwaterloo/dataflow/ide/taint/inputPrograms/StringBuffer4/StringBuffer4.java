package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuffer4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuffer4 {

    public static void main(String[] args) {
       String s1 = "not secret";
       StringBuffer sb1 = new StringBuffer(s1);
       String s2 = secret();
       StringBuffer sb2 = new StringBuffer(s2);
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

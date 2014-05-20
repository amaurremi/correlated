package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuffer3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuffer3 {

    public static void main(String[] args) {
       String s1 = "not secret";
       StringBuffer sb = new StringBuffer(s1);
       sb.append(secret());
       String s2 = sb.toString();
       SecretAssertions.secret(s2);
    }

    static String f() {
        String s = secret();
        return s;
    }

    static String secret() {
        return "secret";
    }
}

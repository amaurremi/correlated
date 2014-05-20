package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuffer6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuffer6 {

    public static void main(String[] args) {
       String s = secret();
       StringBuffer sb = new StringBuffer(s);
       sb.append('x');
       Object o = new Object();
       sb.append(o);
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

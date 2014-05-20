package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder5 {

    public static void main(String[] args) {
       String s = secret();
       StringBuilder sb = new StringBuilder(s);
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

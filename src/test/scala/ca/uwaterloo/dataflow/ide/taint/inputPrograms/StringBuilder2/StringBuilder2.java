package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder2 {

    public static void main(String[] args) {
       String s1 = "not secret";
       StringBuilder sb = new StringBuilder(s1);
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

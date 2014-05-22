package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuffer7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuffer7 {

    public static void main(String[] args) {
       StringBuffer sb = new StringBuffer();
       String s1 = sb.toString();
       SecretAssertions.notSecret(s1);
       foo(sb);
       String s2 = sb.toString();
       SecretAssertions.secret(s2);
    }

    public static void foo(StringBuffer sb){
      sb.append(secret());
    }

    static String secret() {
        return "secret";
    }
}

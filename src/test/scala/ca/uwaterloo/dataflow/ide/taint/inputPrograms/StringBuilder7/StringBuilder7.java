package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder7 {

    public static void main(String[] args) {
       StringBuilder sb = new StringBuilder();
       String s1 = sb.toString();
       SecretAssertions.notSecret(s1);
       foo(sb);
       String s2 = sb.toString();
       SecretAssertions.secret(s2);
    }

    public static void foo(StringBuilder sb){
      sb.append(secret());
    }

    static String secret() {
        return "secret";
    }
}

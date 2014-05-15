package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Recursion2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Recursion2 {

    public static void main(String[] args) {
        String s1 = secret();
        String s2 = foo(s1, 5);
        SecretAssertions.shouldBeSecret(s2);
        String s3 = "not secret";
        String s4 = foo(s3, 5);
        SecretAssertions.shouldBeSecret(s4); // because the analysis is conservative
    }

    public static String foo(String s, int n){
      String t = s;
      if (n > 0){
        return foo(t, n-1);
      } else {
        return t;
      }
    }

    static String secret() {
        return "secret";
    }
}


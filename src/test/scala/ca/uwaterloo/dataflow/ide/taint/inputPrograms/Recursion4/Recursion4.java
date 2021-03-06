package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Recursion4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Recursion4 {

    public static void main(String[] args) {
        String s1 = secret();
        String s2 = foo(s1, 5);
        SecretAssertions.secret(s2);
        String s3 = "not secret";
        String s4 = bar(s3, 5);
        SecretAssertions.notSecret(s4);
    }

    public static String foo(String s, int n){
      String t = s;
      if (n > 0){
        return bar(t, n-1);
      } else {
        return t;
      }
    }

    public static String bar(String s, int n) {
        String t = s;
        if (n > 0) {
            return foo(t, n - 1);
        } else {
            return t;
        }
    }

    static String secret() {
        return "secret";
    }
}


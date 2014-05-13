package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Recursion;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Recursion {


    public static void main(String[] args) {
        String s1 = secret();
        String s2 = foo(s1, 5);
        SecretAssertions.shouldBeSecret(s2);
    }

    public static String foo(String s, int n){
      if (n > 0){
        return foo(s, n-1);
      } else {
        return s;
      }
    }

    static String secret() {
        return "secret";
    }
}


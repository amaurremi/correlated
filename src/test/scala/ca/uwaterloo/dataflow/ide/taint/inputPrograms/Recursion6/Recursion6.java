package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Recursion6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Recursion6 {

    public static void main(String[] args) {
        A a = new A();
        String s1 = secret();
        String s2 = a.foo(s1, 5);
        SecretAssertions.shouldBeSecret(s2);
        String s3 = "not secret";
        String s4 = a.foo(s3, 5);
        SecretAssertions.shouldBeSecret(s4); // because the analysis is conservative
    }

    static String secret() {
        return "secret";
    }
}

class A {
    public String foo(String s, int n){
      String t = s;
      if (n > 0){
        return foo(t, n-1);
      } else {
        return t;
      }
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Recursion7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Recursion7 {

    public static void main(String[] args) {
        A a = new A();
        String s1 = secret();
        String s2 = a.foo(s1, 5);
        SecretAssertions.shouldBeSecret(s2);
    }

    static String secret() {
        return "secret";
    }
}

class A {

    public String foo(String s, int n){
      String t = s;
      if (n > 0){
        return bar(t, n-1);
      } else {
        return t;
      }
    }

    public String bar(String s, int n) {
        String t = s;
        if (n > 0) {
            return foo(t, n - 1);
        } else {
            return t;
        }
    }

}

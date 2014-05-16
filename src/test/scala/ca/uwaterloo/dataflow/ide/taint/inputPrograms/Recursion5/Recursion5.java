package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Recursion5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Recursion5 {


    public static void main(String[] args) {
        A a = new A();
        String s1 = secret();
        String s2 = a.foo(s1, 5);
        SecretAssertions.secret(s2);
    }

    static String secret() {
        return "secret";
    }
}

class A {
    public String foo(String s, int n){
      if (n > 0){
        return foo(s, n-1);
      } else {
        return s;
      }
    }
}


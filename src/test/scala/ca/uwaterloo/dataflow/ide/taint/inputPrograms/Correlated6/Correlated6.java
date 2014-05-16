package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Correlated6 {

    public static void main(String[] args) {

        A a = null;
        if (args.length == 0){
          a = new A();
        } else {
          a = new B();
        }
 
        String s1 = secret();
        String s3 = a.makeCalls(s1);
        SecretAssertions.secretStandardNotSecretCc(s3);

    }

    static String secret() {
        return "secret";
    }
}

class A {
  String makeCalls(String s){
    String temp = this.foo(s, 5);
    return this.bar(temp);
  }

  String foo(String x, int n){ 
    if (n > 0){
      return this.foo(x, n-1);
    } else {
      return x; 
    }
  }
  String bar(String y){ return "not secret"; }

}

class B extends A {
  String foo(String x, int n){ 
    if (n > 0){
      return foo(x, n-1);
    } else {
      return "not secret"; 
    }
  }
  String bar(String y){ return y; }
}


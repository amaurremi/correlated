package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Correlated5 {

    public static void main(String[] args) {

        A a = null;
        if (args.length == 0){
          a = new A();
        } else {
          a = new C();
        }
 
        String s1 = secret();
        String s3 = a.makeCalls(s1);
        SecretAssertions.shouldBeSecretNonCc(s3);
        SecretAssertions.shouldNotBeSecretCc(s3);

    }

    static String secret() {
        return "secret";
    }
}

class A {
  String makeCalls(String s){
    String temp = this.foo(s);
    return this.bar(temp);
  }

  String foo(String x){ return x; }
  String bar(String y){ return "not secret"; }

}

class B extends A {
  String foo(String x){ return "not secret"; }
}

class C extends B {
  String bar(String y){ return y; }
}

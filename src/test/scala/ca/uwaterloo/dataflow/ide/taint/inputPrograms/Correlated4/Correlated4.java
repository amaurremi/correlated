package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Correlated4 {

    public static void main(String[] args) {

        A a = null;
        if (args.length == 0){
          a = new A();
        } else if (args.length == 1){
          a = new B();
        } else {
          a = new C();
        }
 
        String s1 = secret();
        String s3 = a.makeCalls(s1);
        SecretAssertions.shouldBeSecret(s3);
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
  String bar(String y){ return y; }
}

class C extends A {
  String foo(String x){ return "not secret"; }
  String bar(String y){ return y; }
}

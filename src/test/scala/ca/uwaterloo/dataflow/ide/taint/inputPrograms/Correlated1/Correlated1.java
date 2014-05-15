package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated1;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Correlated1 {

    public static void main(String[] args) {

        A a = (args.length == 1) ? new A() : new B();
        a.foo(secret());
        String s3 = a.bar();
        SecretAssertions.shouldBeSecret(s3);
        SecretAssertions.shouldNotBeSecretCc(s3);

    }

    static String secret() {
        return "secret";
    }
}

class A {
  A(){
      this.x = "not secret";
      this.y = "not secret";
  }
  void foo(String s){ this.x = s; }
  String bar(){ return y; }

  String x, y;
}

class B extends A {
  void foo(String s){ this.y = s; }
  String bar(){ return x; }
}

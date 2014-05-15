package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Correlated2 {

    public static void main(String[] args) {

        A a = (args.length == 1) ? new A() : new B();
 
        String s1 = secret();
        String s2 = a.foo(s1);
        String s3 = a.bar(s2);
        SecretAssertions.shouldBeSecret(s3);
        SecretAssertions.shouldNotBeSecretCc(s3);

    }

    static String secret() {
        return "secret";
    }
}

class A {
  String foo(String x){ return x; }
  String bar(String y){ return "not secret"; }

}

class B extends A {
  String foo(String x){ return "not secret"; }
  String bar(String y){ return y; }
}

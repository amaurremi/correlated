package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated10;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class A {
  public String foo(){ return Correlated10.secret(); }
  public void bar(String s){ }
}
class B extends A {
  public String foo(){ return null; }
  public void bar(String s){
       SecretAssertions.notSecret(s);
  }
}

public class Correlated10 {

    public static void main(String[] args) {
       A x = (args.length == 2) ? new A() : new B();
       String v = x.foo();
       x.bar(v);
    }

    static String secret() {
        return "secret";
    }
}

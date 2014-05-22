package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated8;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class A {
  public String foo(){ return Correlated8.secret(); }
  public void bar(String s){ }
}
class B extends A {
  public String foo(){ return null; }
  public void bar(String s){
       SecretAssertions.secret(s);
  }
}

public class Correlated8 {

    private static String f(A x, A y, String z){
      String v = x.foo();
      String w;
      if (y != null){
        w = f(y, null, v);
      } else {
        w = z;
      }
      x.bar(w);
      return v;
    }

    public static void main(String[] args) {
      f(new A(), new B(), null);
    }

    static String secret() {
        return "secret";
    }
}

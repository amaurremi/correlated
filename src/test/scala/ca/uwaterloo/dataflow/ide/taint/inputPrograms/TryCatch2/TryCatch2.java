package ca.uwaterloo.dataflow.ide.taint.inputPrograms.TryCatch2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class TryCatch2 {

    static class MyException extends Exception {
      public MyException(String s){
        this.p1 = s;
        this.p2 = "not secret";
      }
      public String get1(){
        return p1;
      }
      public String get2(){
            return p2;
        }
      private String p1;
      private String p2;
    }

    public static void main(String[] args) {
        try {
          throw new MyException(secret());
        } catch(MyException e) {
            String s1 = e.get1();
            SecretAssertions.secret(s1);
            String s2 = e.get2();
            SecretAssertions.notSecret(s2);
        } 
    }

    static String secret() {
        return "secret";
    }
}

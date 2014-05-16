package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionCall3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionCall3 {

    public static void main(String[] args) {
        X x = new X();
        String s = secret();
        String t = "notsecret";
        x.f(s, t);
    }

    private static class X {
         public String f(String s1, String s2) {
            SecretAssertions.secret(s1);
            SecretAssertions.notSecret(s2);
            return s1;
        }
    }



    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionCall2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionCall2 {

    public static void main(String[] args) {
        X x = new X();
        String s = secret();
        x.f(s);
    }

    private static class X {
         public String f(String s) {
            SecretAssertions.secret(s);
            return s;
        }
    }

    static String secret() {
        return "secret";
    }
}

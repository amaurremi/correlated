package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn6 {

    public static void main(String[] args) {
       String s = (new X()).f();
       SecretAssertions.notSecret(s);
    }

    static class X {
        public String f() {
            String s = "not secret";
            return s;
        }
    }

    static String secret() {
        return "secret";
    }
}

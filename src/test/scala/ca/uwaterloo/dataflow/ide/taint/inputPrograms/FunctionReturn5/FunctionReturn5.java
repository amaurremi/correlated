package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn5 {

    public static void main(String[] args) {
       String s = (new X()).f();
       SecretAssertions.secret(s);
    }

    static class X {
        public String f() {
            String s = secret();
            return s;
        }
    }

    static String secret() {
        return "secret";
    }
}

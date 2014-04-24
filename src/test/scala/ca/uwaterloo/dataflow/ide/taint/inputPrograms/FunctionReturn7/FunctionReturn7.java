package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn7 {

    public static void main(String[] args) {
       String s = (new X()).f(args.length);
       SecretAssertions.shouldBeSecret(s);
    }

    static class X {
        static String f(int x) {
            if (x == 0){
                return "not secret";
            } else {
                return secret();
            }
        }
    }

    static String secret() {
        return "secret";
    }
}

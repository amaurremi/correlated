package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionCall;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionCall {

    public static void main(String[] args) {
       String s = secret();
       f(s);
    }

    static String f(String s) {
        SecretAssertions.shouldBeSecret(s);
        return s;
    }

    static String secret() {
        return "secret";
    }
}

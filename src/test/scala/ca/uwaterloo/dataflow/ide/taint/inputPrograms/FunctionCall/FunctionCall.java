package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionCall;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionCall {

    public static void main(String[] args) {
       String s = secret();
       f(s);
       SecretAssertions.secret(s);
    }

    static String f(String s) {
        SecretAssertions.secret(s);
        return s;
    }

    static String secret() {
        return "secret";
    }
}

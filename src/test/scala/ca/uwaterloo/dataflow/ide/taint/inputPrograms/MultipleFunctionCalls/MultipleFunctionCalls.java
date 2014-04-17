package ca.uwaterloo.dataflow.ide.taint.inputPrograms.MultipleFunctionCalls;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class MultipleFunctionCalls{

    public static void main(String[] args) {
       String secret = secret();
       String notSecret = "not secret";
       f(secret);
       f(notSecret);
    }

    static String f(String s) {
        SecretAssertions.shouldBeSecret(s);
        return s;
    }

    static String secret() {
        return "secret";
    }
}

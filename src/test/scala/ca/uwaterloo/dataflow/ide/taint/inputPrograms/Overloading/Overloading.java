package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Overloading;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Overloading {

    private static String foo(int x){ return "not secret"; }
    private static String foo(boolean x){ return secret(); }

    public static void main(String[] args) {
        String s = foo(3);
        SecretAssertions.shouldNotBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

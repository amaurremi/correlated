package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn2 {

    public static void main(String[] args) {
       String s = f();
       SecretAssertions.shouldNotBeSecret(s);
    }

    static String f() {
        String s = "not secret";
        return s;
    }

    static String secret() {
        return "secret";
    }
}

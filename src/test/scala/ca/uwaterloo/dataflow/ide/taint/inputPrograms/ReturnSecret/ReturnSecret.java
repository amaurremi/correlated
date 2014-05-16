package ca.uwaterloo.dataflow.ide.taint.inputPrograms.ReturnSecret;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class ReturnSecret {

    public static void main(String[] args) {
        String s = f();
        SecretAssertions.secret(s);
    }

    static String f() {
        return secret();
    }

    static String secret() {
        return "secret";
    }
}

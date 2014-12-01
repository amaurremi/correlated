package ca.uwaterloo.dataflow.ide.taint.inputPrograms.ReturnFromConstructor;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class ReturnFromConstructor {

    public static void main(String[] args) {
        new ReturnFromConstructor();
        String secret = secret();
        SecretAssertions.secret(secret);
    }

    static String secret() {
        return "secret";
    }
}

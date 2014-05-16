package ca.uwaterloo.dataflow.ide.taint.inputPrograms.LocalVars;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class LocalVars {

    public static void main(String[] args) {
        String x = secret();
        SecretAssertions.secret(x);
    }

    static String secret() {
        return "secret";
    }
}

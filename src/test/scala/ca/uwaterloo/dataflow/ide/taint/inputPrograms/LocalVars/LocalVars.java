package ca.uwaterloo.dataflow.ide.taint.inputPrograms.LocalVars;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class LocalVars extends SecretAssertions {

    public static void main(String[] args) {
        String x = secret();
        SecretAssertions.shouldBeSecret(x);
    }

    static String secret() {
        return "secret";
    }
}

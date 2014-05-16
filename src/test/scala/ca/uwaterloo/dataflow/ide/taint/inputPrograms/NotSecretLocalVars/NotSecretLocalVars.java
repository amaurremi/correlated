package ca.uwaterloo.dataflow.ide.taint.inputPrograms.NotSecretLocalVars;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class NotSecretLocalVars {

    public static void main(String[] args) {
        String x = notSecret();
        int y = 1;
        String z = secret();

        SecretAssertions.notSecret(x);
        SecretAssertions.notSecret(y);
        SecretAssertions.secret(z);
    }

    static String notSecret() {
        return "not secret";
    }

    static String secret() {
        return "secret";
    }
}

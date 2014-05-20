package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringConcat2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringConcat2 {

    public static void main(String[] args) {
        String s = secret();
        String sns = s + "not secret";
        SecretAssertions.secret(sns);
        SecretAssertions.secret("not secret" + s);
        SecretAssertions.secret(sns + "not secret");
        StringBuilder stringBuilder = new StringBuilder("not secret");
        SecretAssertions.notSecret(stringBuilder);
        StringBuilder appendSecret = stringBuilder.append(secret());
        SecretAssertions.secret(appendSecret);
        SecretAssertions.secret(appendSecret.toString());
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Ternary;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Ternary {

    public static void main(String[] args) {
        String s = secret();
        String t = "not secret";
        String r = (args.length == 0) ? s : t;

        SecretAssertions.secret(s);
        SecretAssertions.notSecret(t);
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

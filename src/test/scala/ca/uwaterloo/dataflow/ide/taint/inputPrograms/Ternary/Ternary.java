package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Ternary;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Ternary {

    public static void main(String[] args) {
        String s = secret();
        String t = "not secret";
        String r = (args.length == 0) ? s : t;

        SecretAssertions.shouldBeSecret(s);
        SecretAssertions.shouldNotBeSecret(t);
        SecretAssertions.shouldBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

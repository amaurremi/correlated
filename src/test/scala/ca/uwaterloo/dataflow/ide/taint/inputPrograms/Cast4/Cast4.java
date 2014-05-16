package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Cast4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// cast up to interface CharSequence, then down to String
public class Cast4 {

    public static void main(String[] args) {
        String s = "not secret";
        SecretAssertions.notSecret(s);
        CharSequence c = s;
        String s2 = (String)c;
        SecretAssertions.notSecret(s2);
    }

    static String secret() {
        return "secret";
    }
}

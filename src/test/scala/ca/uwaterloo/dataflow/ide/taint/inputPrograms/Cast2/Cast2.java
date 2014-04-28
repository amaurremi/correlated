package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Cast2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// cast up to interface CharSequence, then down to String
public class Cast2 {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.shouldBeSecret(s);
        CharSequence c = s;
        String s2 = (String)c;
        SecretAssertions.shouldBeSecret(s2);
    }

    static String secret() {
        return "secret";
    }
}

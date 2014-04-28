package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Cast3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// cast up to class Object, then down to String
public class Cast3 {

    public static void main(String[] args) {
        String s = "not secret";
        SecretAssertions.shouldNotBeSecret(s);
        Object o = s;
        String s2 = (String)o;
        SecretAssertions.shouldNotBeSecret(s2);
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Cast6;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// scenario with failing cast
public class Cast6 {

    public static void main(String[] args) {
        Object o = new Integer(7);
        String s = (String)o; // cast fails at run-time
        SecretAssertions.shouldNotBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

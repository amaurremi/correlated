package ca.uwaterloo.dataflow.ide.taint.inputPrograms.If;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class If {

    public static void main(String[] args) {
        String s = secret();
        if (args != null) {
            s = "not secret";
        } else {
            s = "not secret";
        }
        SecretAssertions.shouldNotBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

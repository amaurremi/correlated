package ca.uwaterloo.dataflow.ide.taint.inputPrograms.If4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class If4 {

    public static void main(String[] args) {
        String s = "not secret";
        if (args != null) {
            s = secret();
        } else {
            s = secret();
        }
        SecretAssertions.shouldBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

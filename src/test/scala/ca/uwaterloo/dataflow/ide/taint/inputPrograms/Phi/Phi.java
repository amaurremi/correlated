package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Phi;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Phi {

    public static void main(String[] args) {
        String s;
        if (args != null) {
            s = secret();
        } else {
            s = "not secret";
        }
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

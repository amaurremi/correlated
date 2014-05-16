package ca.uwaterloo.dataflow.ide.taint.inputPrograms.PhiSame;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class PhiSame {

    public static void main(String[] args) {
        String s;
        if (args != null) {
            s = secret();
        } else {
            s = secret();
        }
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

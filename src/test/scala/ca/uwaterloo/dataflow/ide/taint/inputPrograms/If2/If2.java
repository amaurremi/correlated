package ca.uwaterloo.dataflow.ide.taint.inputPrograms.If2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// if with one branch
public class If2 {

    public static void main(String[] args) {
        String s = secret();
        if (args != null) {
            s = "not secret";
        }
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

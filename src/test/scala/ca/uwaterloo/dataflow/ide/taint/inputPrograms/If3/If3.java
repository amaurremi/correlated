package ca.uwaterloo.dataflow.ide.taint.inputPrograms.If3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// if with one branch
public class If3 {

    public static void main(String[] args) {
        String s = "not secret";
        if (args != null) {
            s = secret();
        } 
        SecretAssertions.shouldBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

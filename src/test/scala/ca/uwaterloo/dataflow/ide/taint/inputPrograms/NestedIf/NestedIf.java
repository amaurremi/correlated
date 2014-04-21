package ca.uwaterloo.dataflow.ide.taint.inputPrograms.NestedIf;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class NestedIf {

    public static void main(String[] args) {
        String s = "not secret";
        if (args != null) {
            if (args.length > 5){
                s = "not secret";
            } else {
                s = secret();
            }
        } else {
            s = "not secret";
        }
        SecretAssertions.shouldBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

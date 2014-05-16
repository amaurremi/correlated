package ca.uwaterloo.dataflow.ide.taint.inputPrograms.NestedIf2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class NestedIf2 {

    public static void main(String[] args) {
        String s = "not secret";
        String t = secret();
        if (args != null) {
            SecretAssertions.notSecret(s);
            if (args.length > 5){
                s = "not secret";
            } else {
                s = t;
            }
        } else {
            s = "not secret";
        }
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

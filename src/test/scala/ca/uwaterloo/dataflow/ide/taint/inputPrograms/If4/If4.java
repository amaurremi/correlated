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
        SecretAssertions.secret(s);
        String t = secret();
        String a;
        if (args == null) {
            a = t;
        } else {
            a = "not secret";
        }
        SecretAssertions.secret(a);
    }

    static String secret() {
        return "secret";
    }
}

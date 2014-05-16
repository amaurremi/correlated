package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Return;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Return {

    public static void main(String[] args) {
        String s = f();
        SecretAssertions.secret(s);
    }

    static String f() {
        String s = secret();
        return s;
    }

    static String secret() {
        return "secret";
    }
}

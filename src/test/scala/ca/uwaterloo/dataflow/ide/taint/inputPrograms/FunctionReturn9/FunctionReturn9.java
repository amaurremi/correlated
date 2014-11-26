package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn9;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn9 {

    public static void main(String[] args) {
        f(secret());
        String s2 = f(secret());
        SecretAssertions.secret(s2);
    }

    static String f(String s) {
        return s;
    }

    static String secret() {
        return "secret";
    }
}

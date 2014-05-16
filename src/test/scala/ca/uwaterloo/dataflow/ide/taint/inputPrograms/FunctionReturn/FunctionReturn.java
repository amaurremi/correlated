package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn {

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

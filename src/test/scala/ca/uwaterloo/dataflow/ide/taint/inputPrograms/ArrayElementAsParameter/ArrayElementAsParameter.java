package ca.uwaterloo.dataflow.ide.taint.inputPrograms.ArrayElementAsParameter;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class ArrayElementAsParameter {

    public static void main(String[] args) {
        args[0] = secret();
        f(args[0]);
    }

    static String secret() {
        return "secret";
    }

    static void f(String string) {
        SecretAssertions.secret(string);
    }
}

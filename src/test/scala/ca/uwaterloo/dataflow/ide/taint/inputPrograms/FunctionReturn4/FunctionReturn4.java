package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn4 {

    public static void main(String[] args) {
       String s1 = f(secret());
       SecretAssertions.secret(s1);
       String s2 = f("not secret");
       SecretAssertions.notSecret(s2); // it will be secret because f can be invoked with a secret value
    }

    static String f(String s3) {
       return s3;
    }

    static String secret() {
        return "secret";
    }
}

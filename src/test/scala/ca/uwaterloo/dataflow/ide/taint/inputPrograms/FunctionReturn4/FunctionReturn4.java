package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn4 {

    public static void main(String[] args) {
       String s1 = f(secret());
       SecretAssertions.shouldBeSecret(s1);
       String s2 = f("not secret");
       SecretAssertions.shouldNotBeSecret(s2);
    }

    static String f(String s3) {
       return s3;
    }

    static String secret() {
        return "secret";
    }
}

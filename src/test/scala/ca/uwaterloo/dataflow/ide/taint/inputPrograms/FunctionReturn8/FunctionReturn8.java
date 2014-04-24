package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn8;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn8 {

    public static void main(String[] args) {
       String s1 = (new X()).f(secret());
       SecretAssertions.shouldBeSecret(s1);
       String s2 = (new X()).f("not secret");
       SecretAssertions.shouldNotBeSecret(s2);
    }

    static class X {
        public String f(String s3) {
           return s3;
        }
    }

    static String secret() {
        return "secret";
    }
}

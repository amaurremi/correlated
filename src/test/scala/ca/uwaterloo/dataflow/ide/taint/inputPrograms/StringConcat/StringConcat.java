package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringConcat;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringConcat {

    public static void main(String[] args) {
       String s1 = secret();
       String s2 = "not secret";
       String s3 = s1 + s2;
       SecretAssertions.shouldBeSecret(s3);
    }

    static String f() {
        String s = secret();
        return s;
    }

    static String secret() {
        return "secret";
    }
}

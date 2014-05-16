package ca.uwaterloo.dataflow.ide.taint.inputPrograms.TryCatch;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class TryCatch {

    public static void main(String[] args) {
        try {
            String s1 = secret();
            SecretAssertions.secret(s1);
            SecretAssertions.notSecret("not secret");
        } catch(Exception e) {
            String s2 = secret();
            SecretAssertions.secret(s2);
            SecretAssertions.notSecret("not secret");
        } finally {
            String s3 = secret();
            SecretAssertions.secret(s3);
            SecretAssertions.notSecret("not secret");
        }
    }

    static String secret() {
        return "secret";
    }
}

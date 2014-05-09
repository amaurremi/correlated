package ca.uwaterloo.dataflow.ide.taint.inputPrograms.TryCatch;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class TryCatch {

    public static void main(String[] args) {
        try {
            String s1 = secret();
            SecretAssertions.shouldBeSecret(s1);
            SecretAssertions.shouldNotBeSecret("not secret");
        } catch(Exception e) {
            String s2 = secret();
            SecretAssertions.shouldBeSecret(s2);
            SecretAssertions.shouldNotBeSecret("not secret");
        } finally {
            String s3 = secret();
            SecretAssertions.shouldBeSecret(s3);
            SecretAssertions.shouldNotBeSecret("not secret");
        }
    }

    static String secret() {
        return "secret";
    }
}

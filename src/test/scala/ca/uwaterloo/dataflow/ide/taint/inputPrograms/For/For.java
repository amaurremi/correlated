package ca.uwaterloo.dataflow.ide.taint.inputPrograms.For;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class For {

    public static void main(String[] args) {
        String s = secret();
        for (int i=0; i < 10; i++){
            s = "not secret";
        }
        SecretAssertions.shouldNotBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

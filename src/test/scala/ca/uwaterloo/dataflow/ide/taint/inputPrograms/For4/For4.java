package ca.uwaterloo.dataflow.ide.taint.inputPrograms.For4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class For4 {

    public static void main(String[] args) {
        String s = secret();
        for (int i = 0; i < 10; i++) {
            s = "not secret";
            if (i > 5) {
                continue;
            }
        }
        SecretAssertions.shouldBeSecret(s); // the analysis doesn't even know that we always enter the loop
    }

    static String secret() {
        return "secret";
    }
}

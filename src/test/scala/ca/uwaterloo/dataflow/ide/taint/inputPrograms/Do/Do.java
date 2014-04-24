package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Do;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Do {

    public static void main(String[] args) {
        String s = secret();
        int i=0;
        do {
            s = "not secret";
            i++;
        } while (i < 10);
        SecretAssertions.shouldNotBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

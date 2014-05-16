package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Do2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Do2 {

    public static void main(String[] args) {
        String s = "not secret";
        int i=0;
        do {
            s = secret();
            i++;
        } while (i < 10);
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

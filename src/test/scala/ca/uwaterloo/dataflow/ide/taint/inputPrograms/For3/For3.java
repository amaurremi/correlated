package ca.uwaterloo.dataflow.ide.taint.inputPrograms.For3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class For3 {

    public static void main(String[] args) {
        String s = secret();
        for (int i = 0; i < 10; i++) {
            s = "not secret";
            if (i == 5) {
                break;
            }
        }
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

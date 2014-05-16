package ca.uwaterloo.dataflow.ide.taint.inputPrograms.For2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class For2 {

    public static void main(String[] args) {
        String s = "not secret";
        for (int i=0; i < 10; i++){
            s = secret();
        }
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

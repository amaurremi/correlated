package ca.uwaterloo.dataflow.ide.taint.inputPrograms.While2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class While2 {

    public static void main(String[] args) {
        String s = "not secret";
        int i=0;
        while (i < 10){
            s = secret();
            i++;
        }
        SecretAssertions.shouldBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

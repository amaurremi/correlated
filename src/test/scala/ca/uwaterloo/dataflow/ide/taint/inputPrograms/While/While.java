package ca.uwaterloo.dataflow.ide.taint.inputPrograms.While;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class While {

    public static void main(String[] args) {
        String s = secret();
        int i=0;
        while (i < 10){
            s = "not secret";
            i++;
        }
        SecretAssertions.shouldNotBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

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
        SecretAssertions.secret(s); // the analysis doesn't know that we will always enter the loop
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.For4;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class For4 {

    public static void main(String[] args) {
        String s = secret();
        for (int i=0; i < 10; i++){
            s = "not secret";
            if (i > 5){
		continue;
	    }
        }
        SecretAssertions.shouldNotBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

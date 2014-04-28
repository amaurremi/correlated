package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Instanceof2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Instanceof2 {

    public static void main(String[] args) {
        Object o = "not secret";
        if (o instanceof String){
          String s = (String)o; 
          SecretAssertions.shouldNotBeSecret(s);
        }
    }

    static String secret() {
        return "secret";
    }
}

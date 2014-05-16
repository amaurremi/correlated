package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Instanceof;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Instanceof {

    public static void main(String[] args) {
        Object o = secret();
        if (o instanceof String){
          String s = (String)o; 
          SecretAssertions.secret(s);
        }
    }

    static String secret() {
        return "secret";
    }
}

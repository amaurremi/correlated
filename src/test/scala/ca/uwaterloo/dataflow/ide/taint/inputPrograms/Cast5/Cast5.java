package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Cast5;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Cast5 {

    public static void main(String[] args) {
        CharSequence x = null;
        if (args.length == 0){
           x = secret();
        } else {
           x = "not secret";
        }
        String s = (String)x;
        SecretAssertions.secret(s);
    }

    static String secret() {
        return "secret";
    }
}

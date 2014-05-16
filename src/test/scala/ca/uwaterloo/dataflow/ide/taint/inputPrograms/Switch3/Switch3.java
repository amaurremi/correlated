package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Switch3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// switch with fall-through
public class Switch3 {

    public static void main(String[] args) {
        String s;
        switch (args.length){
            case 0: s = secret();
              // note that we are falling through to the default branch
            default:
               s = "not secret";
        }
        SecretAssertions.notSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

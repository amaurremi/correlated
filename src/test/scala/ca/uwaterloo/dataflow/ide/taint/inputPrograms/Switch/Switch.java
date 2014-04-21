package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Switch;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Switch {

    public static void main(String[] args) {
        String s;
        switch (args.length){
            case 0: s = secret();
                break;
            case 1: s = "not secret";
                break;
            default:
               s = "not secret";
        }
        SecretAssertions.shouldBeSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

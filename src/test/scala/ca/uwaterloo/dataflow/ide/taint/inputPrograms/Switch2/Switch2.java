package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Switch2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// the secret value is overwritten on all branches of the switch
public class Switch2 {

    public static void main(String[] args) {
        String s = secret();
        switch (args.length){
            case 0: s = "not secret";
                break;
            default:
               s = "not secret";
        }
        SecretAssertions.notSecret(s);
    }

    static String secret() {
        return "secret";
    }
}

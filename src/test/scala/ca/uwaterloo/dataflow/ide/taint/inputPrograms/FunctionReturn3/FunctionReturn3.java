package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionReturn3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class FunctionReturn3 {

    public static void main(String[] args) {
       String s = f(args.length);
       SecretAssertions.shouldBeSecret(s);
    }

    static String f(int x) {
        if (x == 0){
            return "not secret";
        } else {
            return secret();
        }
    }

    static String secret() {
        return "secret";
    }
}

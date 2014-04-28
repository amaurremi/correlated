package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field3;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    String f;
}

public class Field3 {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.shouldBeSecret(s);
        X x = new X();
        x.f = s;
        SecretAssertions.shouldBeSecret(x.f);
    }

    static String secret() {
        return "secret";
    }
}

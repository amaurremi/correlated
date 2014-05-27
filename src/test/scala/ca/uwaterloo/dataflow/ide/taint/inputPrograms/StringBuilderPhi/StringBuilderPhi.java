package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilderPhi;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilderPhi {

    public static void main(String[] args) {
        StringBuilder sb1 = new StringBuilder();
        Object o = new Object();
        Object sb = args == null ? sb1 : o;
        SecretAssertions.secret(sb);
//        SecretAssertions.secret(sb1);
//        SecretAssertions.secret(o);

//        StringBuilder sb2 = new StringBuilder();
//        SecretAssertions.notSecret(sb2);
//        sb2.append(secret());
//        SecretAssertions.secret(sb2);
    }

    static String secret() {
        return "secret";
    }
}

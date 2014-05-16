package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Cast;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

// cast up to class Object, then down to String
public class Cast {

    public static void main(String[] args) {
        String s = secret();
        SecretAssertions.secret(s);
        Object o = s;
        String s2 = (String) o;
        SecretAssertions.secret(s2);
    }

    static String secret() {
        return "secret";
    }
}

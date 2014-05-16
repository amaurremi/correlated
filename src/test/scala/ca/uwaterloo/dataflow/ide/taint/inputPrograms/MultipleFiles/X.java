package ca.uwaterloo.dataflow.ide.taint.inputPrograms.MultipleFiles;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String f(String s1, String s2) {
        SecretAssertions.secret(s1);
        SecretAssertions.notSecret(s2);
        return s1;
    }
}

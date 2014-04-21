package ca.uwaterloo.dataflow.ide.taint.inputPrograms.MultipleFiles;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class X {
    public String f(String s1, String s2) {
        SecretAssertions.shouldBeSecret(s1);
        SecretAssertions.shouldNotBeSecret(s2);
        return s1;
    }
}

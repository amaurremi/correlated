package ca.uwaterloo.dataflow.ide.taint.inputPrograms.MainArgsArray;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class MainArgsArray {
    public static void main(String[] args) {
        SecretAssertions.secret(args[0]);
    }
}

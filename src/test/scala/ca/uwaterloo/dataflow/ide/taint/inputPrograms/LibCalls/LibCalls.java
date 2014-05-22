package ca.uwaterloo.dataflow.ide.taint.inputPrograms.LibCalls;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

import java.io.File;
import java.util.HashSet;

class A {
    String s;
}

public class LibCalls {

    public static void main(String[] args) {
        SecretAssertions.secret(new File("").getAbsolutePath()); // java.lang String
        SecretAssertions.secret(String.valueOf(true)); // java.lang static String
        SecretAssertions.secret(new HashSet<Object>().toString()); // java.util String
        SecretAssertions.notSecret(new File("")); // java.lang not String
        SecretAssertions.notSecret(new A().s); // non-library String
    }
}

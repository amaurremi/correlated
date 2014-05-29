package ca.uwaterloo.dataflow.ide.taint.inputPrograms.LibCalls;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

import java.io.File;
import java.util.HashSet;

class A {
    String s;
}

public class LibCalls {

    public static void main(String[] args) {
        String absolutePath = new File("").getAbsolutePath();
        SecretAssertions.secret(absolutePath); // java.lang String
        SecretAssertions.notSecret(String.valueOf(true)); // library method that takes secret argument
        SecretAssertions.secret(String.valueOf(absolutePath)); // library method that takes secret argument
        SecretAssertions.secret(System.getProperty("property")); // java.lang static method
        SecretAssertions.notSecret(String.copyValueOf(null)); // library method in whitelist
        SecretAssertions.secret(new HashSet<Object>().toString()); // java.util String
        SecretAssertions.notSecret(new File("")); // java.lang not String
        SecretAssertions.notSecret(new A().s); // non-library String
    }
}

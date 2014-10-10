package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Primitive;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

import java.io.FileInputStream;
import java.io.IOException;

public class Primitive {

    public static void main(String[] args) {
        try {
            int r = new FileInputStream("file").read();
            SecretAssertions.notSecret(r);
            int c = new FileInputStream("file").available();
            SecretAssertions.notSecret(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

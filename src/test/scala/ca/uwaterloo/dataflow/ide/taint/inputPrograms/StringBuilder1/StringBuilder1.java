package ca.uwaterloo.dataflow.ide.taint.inputPrograms.StringBuilder1;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class StringBuilder1 {

    StringBuilder sb;
    static StringBuilder sb2;

    public static void main(String[] args) {
        StringBuilder1 sb1 = new StringBuilder1();
        sb1.sb = new StringBuilder("not secret");
        SecretAssertions.secret(sb1.sb);
        sb2 = new StringBuilder();
        SecretAssertions.secret(sb2);
    }
}

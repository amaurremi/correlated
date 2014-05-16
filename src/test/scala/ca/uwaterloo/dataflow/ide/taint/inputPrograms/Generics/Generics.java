package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Generics;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

import java.util.ArrayList;
import java.util.List;

public class Generics {

    public static void main(String[] args) {
        String s = secret();
        List<String> list = new ArrayList<String>();
        list.add(s);
        String s2 = list.get(0);
        SecretAssertions.secret(s2);
    }

    static String secret() {
        return "secret";
    }
}

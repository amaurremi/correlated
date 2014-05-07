package ca.uwaterloo.dataflow.ide.taint.inputPrograms.MultipleFiles;

// a version of FunctionCall3 that involves multiple files
public class MultipleFiles {

    public static void main(String[] args) {
        X x = new X();
        String s = secret();
        String t = "notsecret";
        x.f(s, t);
    }

    static String secret() {
        return "secret";
    }
}

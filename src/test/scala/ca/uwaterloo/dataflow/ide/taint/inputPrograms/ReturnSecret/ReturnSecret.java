package ca.uwaterloo.dataflow.ide.taint.inputPrograms.ReturnSecret;

public class ReturnSecret {

    public static void main(String[] args) {
        String s = f();
    }

    static String f() {
        return secret();
    }

    static String secret() {
        return "secret";
    }
}

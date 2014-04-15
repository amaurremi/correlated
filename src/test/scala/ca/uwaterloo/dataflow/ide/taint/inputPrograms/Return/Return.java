package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Return;

public class Return {

    public static void main(String[] args) {
        String s = f();
    }

    static String f() {
        String s = secret();
        return s;
    }

    static String secret() {
        return "secret";
    }
}

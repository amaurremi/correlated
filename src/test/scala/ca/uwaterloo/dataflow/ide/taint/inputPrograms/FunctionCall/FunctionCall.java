package ca.uwaterloo.dataflow.ide.taint.inputPrograms.FunctionCall;

public class FunctionCall{

    public static void main(String[] args) {
       String s = secret();
       f(s);
    }

    static String f(String s) {
        return s;
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.ide.taint.inputPrograms.LocalVars;

public class LocalVars {

    public static void main(String[] args) {
        String x = secret();
        args[0] = x;
    }

    static String secret() {
        return "secret";
    }
}

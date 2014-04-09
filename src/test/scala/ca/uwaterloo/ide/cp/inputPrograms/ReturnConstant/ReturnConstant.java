package ca.uwaterloo.ide.cp.inputPrograms.ReturnConstant;

public class ReturnConstant {

    public static void main(String[] args) {
        args[1] = f();
        String x = args[1];
    }

    static String f() {
        return "1";
    }
}

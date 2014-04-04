package ca.uwaterloo.ide.cp.inputPrograms.ReturnConstant;

public class ReturnConstant {

    public static void main(String[] args) {
        args[1] = f();
        return;
    }

    static String f() {
        return "1";
    }
}

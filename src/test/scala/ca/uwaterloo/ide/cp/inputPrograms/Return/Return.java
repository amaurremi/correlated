package ca.uwaterloo.ide.cp.inputPrograms.Return;

public class Return {

    public static void main(String[] args) {
        args[1] = f(args);
        return;
    }

    static String f(String[] args) {
        args[0] = "0";
        return args[0];
    }
}

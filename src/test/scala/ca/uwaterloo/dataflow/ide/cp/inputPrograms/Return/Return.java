package ca.uwaterloo.dataflow.ide.cp.inputPrograms.Return;

public class Return {

    public static void main(String[] args) {
        args[1] = f(args);
        String x = args[1];
    }

    static String f(String[] args) {
        args[0] = "0";
        return args[0];
    }
}

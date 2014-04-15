package ca.uwaterloo.dataflow.ide.cp.inputPrograms.Phi;

public class Phi {

    public static void main(String[] args) {
        if (args.length > 0) {
            args[0] = "true";
        } else {
            args[0] = "false";
        }
        String x = args[0];
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Phi;

public class Phi {

    public static void main(String[] args) {
        String s;
        if (args != null) {
            s = secret();
        } else {
            s = "not secret";
        }
        args[0] = s; // we need to "use" s, otherwise the phi instructions won't be created
    }

    static String secret() {
        return "secret";
    }
}

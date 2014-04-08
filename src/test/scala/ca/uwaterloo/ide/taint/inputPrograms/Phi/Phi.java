package ca.uwaterloo.ide.taint.inputPrograms.Phi;

public class Phi {

    public static void main(String[] args) {
        String s;
        if (args.length > 0) {
            s = secret();
        } else {
            s = "not secret";
        }
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.ide.taint.inputPrograms.PhiSame;

public class PhiSame {

    public static void main(String[] args) {
        String s;
        if (args.length > 0) {
            s = secret();
        } else {
            s = secret();
        }
    }

    static String secret() {
        return "secret";
    }
}

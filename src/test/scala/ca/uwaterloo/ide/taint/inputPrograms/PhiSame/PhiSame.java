package ca.uwaterloo.ide.taint.inputPrograms.PhiSame;

public class PhiSame {

    public static void main(String[] args) {
        String s;
        if (args != null) {
            s = secret();
        } else {
            s = secret();
        }
        args[0] = s; // we need to "use" s, otherwise the phi instructions won't be created
    }

    static String secret() {
        return "secret";
    }
}

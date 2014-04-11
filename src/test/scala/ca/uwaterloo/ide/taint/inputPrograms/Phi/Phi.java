package ca.uwaterloo.ide.taint.inputPrograms.Phi;

public class Phi {

    public static void main(String[] args) {
        if (true) {
            String s = secret();
        }
    }

    static String secret() {
        return "secret";
    }
}

package ca.uwaterloo.dataflow.ide.taint.inputPrograms.NotSecretLocalVars;

public class NotSecretLocalVars {

    public static void main(String[] args) {
        String x = notSecret();
        int y = 1;
        String z = secret();
    }

    static String notSecret() {
        return "not secret";
    }

    static String secret() {
        return "secret";
    }
}

package LocalVars;

public class LocalVars {

    public static void main(String[] args) {
        String x = secret();
        print(x);
    }

    static String secret() {
        return "secret";
    }

    static void print(String x) {}
}

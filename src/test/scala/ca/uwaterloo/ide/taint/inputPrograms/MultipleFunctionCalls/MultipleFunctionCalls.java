package MultipleFunctionCalls;

public class MultipleFunctionCalls{

    public static void main(String[] args) {
       String secret = secret();
       String notSecret = "not secret";
       f(secret);
       f(notSecret);
    }

    static String f(String s) {
        return s;
    }

    static String secret() {
        return "secret";
    }
}

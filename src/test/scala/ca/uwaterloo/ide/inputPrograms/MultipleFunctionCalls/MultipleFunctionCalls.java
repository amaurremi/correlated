package MultipleFunctionCalls;

public class MultipleFunctionCalls{

    public static void main(String[] args) {
       args[0] = "0";
       args[1] = "1";
       f(args[0]);
       f(args[1]);
    }

    static String f(String s) {
        return s;
    }
}

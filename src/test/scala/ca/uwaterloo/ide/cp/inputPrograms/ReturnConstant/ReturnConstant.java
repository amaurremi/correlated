package ReturnConstant;

public class ReturnConstant {

    public static void main(String[] args) {
        args[1] = f();
    }

    static String f() {
        return "1";
    }
}

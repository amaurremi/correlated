package LocalVars;

public class LocalVars {

    public static void main(String[] args) {
       args[1] = "0";
       f(args[1]);
    }

    static String f(String s) {
        return s;
//      int[] f2 = new int[10];
//      f2[0] = s;
    }
}

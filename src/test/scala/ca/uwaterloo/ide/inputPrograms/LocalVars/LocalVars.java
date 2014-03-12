package LocalVars;

public class LocalVars {

    public static void main(String[] args) {
       int[] f = new int[10];
       f[0] = 1;
       f(f[0]);
    }

    static void f(int s) {
//      int[] f2 = new int[10];
//      f2[0] = s;
    }
}

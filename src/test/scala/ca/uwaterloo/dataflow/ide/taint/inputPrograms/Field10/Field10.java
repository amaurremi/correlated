package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Field10;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Field10 {


    public static void main(String[] args) {
        A a = new A();
        B b = new B();
        SecretAssertions.secret(B.bs);
    }

    static String secret() {
        return "secret";
    }
}

class A {
    static String as = Field10.secret();
}

class B {
    static String bs = A.as;
}

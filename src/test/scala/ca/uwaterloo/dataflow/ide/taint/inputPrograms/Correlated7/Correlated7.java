package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Correlated7;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

public class Correlated7 {

    public static void main(String[] args) {
        A a = args == null ? new A() : new B();
        a.cc1();
        a.cc2();
        String s = a.foo();
        String bar = a.bar(s);
        SecretAssertions.secret(bar);
    }
}

class A {

    String foo() {
        return secret();
    }

    void cc1() {}

    void cc2() {}

    static String secret() {
        return "secret";
    }

    String bar(String a) {
        return "hello";
    }
}

class B extends A {

    void cc1() {}

    void cc2() {}

    String bar(String a) {
        return a;
    }
}

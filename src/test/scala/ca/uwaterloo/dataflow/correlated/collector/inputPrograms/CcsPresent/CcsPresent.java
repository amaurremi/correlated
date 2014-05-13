package ca.uwaterloo.dataflow.correlated.collector.inputPrograms.CcsPresent;

public class CcsPresent {

    public static void main(String[] args) {
        CcsPresent ccsPresent;
        if (args.length > 0) {
            ccsPresent = new CcsPresent();
        } else {
            ccsPresent = new A();
        }
        // first correlated calls
        ccsPresent.bar();
        ccsPresent.bar();
        ccsPresent.foo();
    }

    void bar() {}
    void foo() {}
}

class A extends CcsPresent {

    @Override
    void bar() {}
    @Override
    void foo() {}
}

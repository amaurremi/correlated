package ca.uwaterloo.dataflow.ide.taint.inputPrograms.Generics2;

import ca.uwaterloo.dataflow.ide.taint.inputPrograms.SecretAssertions;

class Cell<T> {
  public void set(T t){ elem = t; }
  public T get(){ return elem; }
  private T elem;
}

public class Generics2 {

    public static void main(String[] args) {
        String s = secret();
        Cell<String> cell = new Cell<String>();
        cell.set(s);
        String s2 = cell.get();
        SecretAssertions.secret(s2);
    }

    static String secret() {
        return "secret";
    }
}

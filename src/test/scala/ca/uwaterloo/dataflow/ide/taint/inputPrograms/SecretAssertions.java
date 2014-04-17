package ca.uwaterloo.dataflow.ide.taint.inputPrograms;

public abstract class SecretAssertions {

    public static void shouldBeSecret(Object o) {}

    public static void shouldNotBeSecret(Object o) {}

    public static void shouldNotBeSecretCC(Object o) {}
}

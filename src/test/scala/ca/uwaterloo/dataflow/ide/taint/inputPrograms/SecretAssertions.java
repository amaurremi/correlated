package ca.uwaterloo.dataflow.ide.taint.inputPrograms;

public abstract class SecretAssertions {

    public static void secret(Object o) {}

    public static void notSecret(Object o) {}

    public static void secretStandardNotSecretCc(Object o) {}
}

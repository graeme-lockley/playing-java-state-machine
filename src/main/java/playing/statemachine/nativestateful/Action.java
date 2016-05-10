package playing.statemachine.nativestateful;

public interface Action<RS> {
    RS apply(RS runtimeState);
}

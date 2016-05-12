package playing.statemachine.classstateful;

public interface Action<RS> {
    RS apply(RS runtimeState);
}

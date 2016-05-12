package playing.statemachine.classstateless;

public interface Action<RS> {
    RS apply(RS runtimeState);
}

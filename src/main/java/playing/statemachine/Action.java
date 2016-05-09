package playing.statemachine;

public interface Action<RS> {
    RS apply(RS runtimeState);
}

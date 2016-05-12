package playing.statemachine.classstateful;

public interface EventAction<EVENT, RS> {
    RS apply(RS runtimeState, EVENT evemt);
}


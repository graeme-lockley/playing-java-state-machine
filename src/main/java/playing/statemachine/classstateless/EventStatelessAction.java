package playing.statemachine.classstateless;

public interface EventStatelessAction<EVENT> {
    void apply(EVENT event);
}


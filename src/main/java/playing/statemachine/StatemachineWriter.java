package playing.statemachine;

public interface StateMachineWriter<STATE, EVENT> {
    STATE initialState();

    Iterable<StateMachineTransition<STATE, EVENT>> transitions();
}

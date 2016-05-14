package playing.statemachine;

public interface StateMachineTransition<STATE, EVENT> {
    STATE fromState();
    STATE toState();
    EVENT event();
}

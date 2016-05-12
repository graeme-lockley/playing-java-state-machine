package playing.statemachine.classstateful;

class Transition<STATE, RS> {
    final STATE fromState;
    final Class eventClass;
    final STATE toState;
    final EventAction<?, RS> action;

    Transition(STATE fromState, Class eventClass, STATE toState, EventAction<?, RS> action) {
        this.fromState = fromState;
        this.eventClass = eventClass;
        this.toState = toState;
        this.action = action;
    }
}

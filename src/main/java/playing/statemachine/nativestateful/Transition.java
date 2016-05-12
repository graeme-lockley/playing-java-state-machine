package playing.statemachine.nativestateful;

class Transition<STATE, EVENT, RS> {
    final STATE fromState;
    final EVENT event;
    final STATE toState;
    final Action<RS> action;

    Transition(STATE fromState, EVENT event, STATE toState, Action<RS> action) {
        this.fromState = fromState;
        this.event = event;
        this.toState = toState;
        this.action = action;
    }
}

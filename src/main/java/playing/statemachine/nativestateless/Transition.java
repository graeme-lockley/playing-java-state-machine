package playing.statemachine.nativestateless;

class Transition<STATE, EVENT> {
    final STATE fromState;
    final EVENT event;
    final STATE toState;
    final StatelessAction action;

    Transition(STATE fromState, EVENT event, STATE toState, StatelessAction action) {
        this.fromState = fromState;
        this.event = event;
        this.toState = toState;
        this.action = action;
    }
}

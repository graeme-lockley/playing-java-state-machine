package playing.statemachine;

class Transition<STATE, EVENT, RS> {
    public final STATE fromState;
    public final EVENT event;
    public final STATE toState;
    public final Action<RS> action;

    public Transition(STATE fromState, EVENT event, STATE toState, Action<RS> action) {
        this.fromState = fromState;
        this.event = event;
        this.toState = toState;
        this.action = action;
    }
}

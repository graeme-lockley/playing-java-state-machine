package playing.statemachine.nativestateless;

import playing.statemachine.StateMachineTransition;
import playing.util.VoidConsumer;

class Transition<STATE, EVENT> implements StateMachineTransition<STATE, EVENT> {
    private final STATE fromState;
    private final EVENT event;
    private final STATE toState;
    private final VoidConsumer action;

    Transition(STATE fromState, EVENT event, STATE toState, VoidConsumer action) {
        this.fromState = fromState;
        this.event = event;
        this.toState = toState;
        this.action = action;
    }

    boolean canFire(STATE state, EVENT event) {
        return fromState.equals(state) && this.event.equals(event);
    }

    void actionAccept() {
        action.accept();
    }

    @Override
    public STATE fromState() {
        return fromState;
    }

    @Override
    public STATE toState() {
        return toState;
    }

    @Override
    public EVENT event() {
        return event;
    }
}

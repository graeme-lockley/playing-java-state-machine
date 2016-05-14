package playing.statemachine.nativestateful;

import playing.statemachine.StateMachineTransition;

class Transition<STATE, EVENT, RS> implements StateMachineTransition<STATE, EVENT> {
    private final STATE fromState;
    private final EVENT event;
    private final STATE toState;
    private final Action<RS> action;

    Transition(STATE fromState, EVENT event, STATE toState, Action<RS> action) {
        this.fromState = fromState;
        this.event = event;
        this.toState = toState;
        this.action = action;
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

    boolean canFire(STATE state, EVENT event) {
        return fromState.equals(state) && this.event.equals(event);
    }

    RS actionApply(RS runtimeState) {
        return action.apply(runtimeState);
    }
}

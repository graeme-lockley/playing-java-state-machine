package playing.statemachine.nativestateful;

import playing.statemachine.StateMachineTransition;

import java.util.function.BiPredicate;
import java.util.function.Function;

class Transition<STATE, EVENT, RS> implements StateMachineTransition<STATE, EVENT> {
    private final STATE fromState;
    private final EVENT event;
    private final STATE toState;
    private final BiPredicate<RS, EVENT> condition;
    private final Function<RS, RS> action;

    public Transition(STATE fromState, EVENT event, BiPredicate<RS, EVENT> condition, STATE toState, Function<RS, RS> action) {
        this.fromState = fromState;
        this.event = event;
        this.condition = condition;
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

    boolean canFire(STATE state, RS runtimeState, EVENT event) {
        return fromState.equals(state) && this.event.equals(event) && condition.test(runtimeState, event);
    }

    RS actionApply(RS runtimeState) {
        return action.apply(runtimeState);
    }
}

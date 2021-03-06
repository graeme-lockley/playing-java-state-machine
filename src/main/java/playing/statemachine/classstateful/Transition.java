package playing.statemachine.classstateful;

import playing.statemachine.StateMachineTransition;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

class Transition<STATE, RS> implements StateMachineTransition<STATE, String> {
    private final STATE fromState;
    private final Class eventClass;
    private final BiPredicate<RS, ?> condition;
    private final STATE toState;
    private final BiFunction<RS, ?, RS> action;

    Transition(STATE fromState, Class eventClass, BiPredicate<RS, ?> condition, STATE toState, BiFunction<RS, ?, RS> action) {
        this.fromState = fromState;
        this.eventClass = eventClass;
        this.condition = condition;
        this.toState = toState;
        this.action = action;
    }

    boolean canFire(STATE currentState, RS runtimeState, Object event) {
        if (currentState.equals(fromState) && eventClass.isInstance(event)) {
            BiPredicate<RS, Object> objectCondition = (BiPredicate<RS, Object>) condition;

            if (objectCondition.test(runtimeState, event)) {
                return true;
            }
        }
        return false;
    }

    public RS applyAction(RS runtimeState, Object event) {
        final BiFunction<RS, Object, RS> eventAction = (BiFunction<RS, Object, RS>) action;
        return eventAction.apply(runtimeState, event);
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
    public String event() {
        return eventClass.toString();
    }

}

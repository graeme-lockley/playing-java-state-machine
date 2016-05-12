package playing.statemachine.classstateful;

import java.util.function.BiPredicate;

class Transition<STATE, RS> {
    final STATE fromState;
    final Class eventClass;
    final BiPredicate<RS, ?> condition;
    final STATE toState;
    final EventAction<?, RS> action;

    Transition(STATE fromState, Class eventClass, BiPredicate<RS, ?> condition, STATE toState, EventAction<?, RS> action) {
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
}

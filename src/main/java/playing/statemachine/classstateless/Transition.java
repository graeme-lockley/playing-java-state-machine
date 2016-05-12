package playing.statemachine.classstateless;

import java.util.function.Predicate;

class Transition<STATE> {
    final STATE fromState;
    final Class eventClass;
    final Predicate<?> condition;
    final STATE toState;
    final EventStatelessAction<?> action;

    Transition(STATE fromState, Class eventClass, Predicate<?> condition, STATE toState, EventStatelessAction<?> action) {
        this.fromState = fromState;
        this.eventClass = eventClass;
        this.condition = condition;
        this.toState = toState;
        this.action = action;
    }

    boolean canFire(STATE currentState, Object event) {
        if (currentState.equals(fromState) && eventClass.isInstance(event)) {
            Predicate<Object> objectCondition = (Predicate<Object>) condition;

            if (objectCondition.test(event)) {
                return true;
            }
        }
        return false;
    }
}

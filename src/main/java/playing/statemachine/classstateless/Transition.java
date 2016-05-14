package playing.statemachine.classstateless;

import playing.statemachine.StateMachineTransition;

import java.util.function.Consumer;
import java.util.function.Predicate;

class Transition<STATE> implements StateMachineTransition<STATE, String> {
    private final STATE fromState;
    private final Class eventClass;
    private final Predicate<?> condition;
    private final STATE toState;
    private final Consumer<?> action;

    Transition(STATE fromState, Class eventClass, Predicate<?> condition, STATE toState, Consumer<?> action) {
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

    public void acceptAction(Object event) {
        final Consumer<Object> eventStatelessAction = (Consumer<Object>) action;
        eventStatelessAction.accept(event);
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

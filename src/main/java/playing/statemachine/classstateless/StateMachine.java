package playing.statemachine.classstateless;

import playing.statemachine.StateMachineTransition;
import playing.statemachine.StateMachineWriter;
import playing.util.ArgumentlessConsumer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StateMachine<STATE> implements StateMachineWriter<STATE, String> {
    private static final ArgumentlessConsumer STATELESS_ACTION = () -> {
    };

    private STATE initialState;
    private List<Transition<STATE>> transitions;
    private Map<STATE, ArgumentlessConsumer> onEntryActions;
    private Map<STATE, ArgumentlessConsumer> onExitActions;

    private StateMachine(STATE initialState, List<Transition<STATE>> transitions, Map<STATE, ArgumentlessConsumer> onEntryActions, Map<STATE, ArgumentlessConsumer> onExitActions) {
        this.initialState = initialState;
        this.transitions = transitions;
        this.onEntryActions = onEntryActions;
        this.onExitActions = onExitActions;
    }

    public STATE start() {
        return initialState;
    }

    public STATE process(STATE machineState, Object... events) {
        STATE runningMachineState = machineState;

        for (Object event : events) {
            final Optional<Transition<STATE>> transitionOptional = findTransition(runningMachineState, event);

            if (transitionOptional.isPresent()) {
                final Transition<STATE> transition = transitionOptional.get();
                final ArgumentlessConsumer onExitAction = onExitActions.getOrDefault(transition.fromState(), STATELESS_ACTION);
                final ArgumentlessConsumer onEntryAction = onEntryActions.getOrDefault(transition.toState(), STATELESS_ACTION);

                onExitAction.accept();
                transition.acceptAction(event);
                onEntryAction.accept();

                runningMachineState = transition.toState();
            }
        }

        return runningMachineState;
    }

    private Optional<Transition<STATE>> findTransition(STATE state, Object event) {
        return transitions.stream().filter(t -> t.canFire(state, event)).findFirst();
    }

    @Override
    public STATE initialState() {
        return initialState;
    }

    @Override
    public Iterable<StateMachineTransition<STATE, String>> transitions() {
        return transitions.stream().collect(Collectors.toList());
    }

    public static class Builder<STATE> {
        private STATE initialState;
        private final List<Transition<STATE>> transitions = new ArrayList<>();
        private final Map<STATE, ArgumentlessConsumer> onEntryActions = new HashMap<>();
        private final Map<STATE, ArgumentlessConsumer> onExitActions = new HashMap<>();


        public Builder<STATE> initialState(STATE initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder<STATE> onState(STATE state, Function<OnStateBuilder<STATE>, OnStateBuilder<STATE>> onStateBuilderFunction) {
            onStateBuilderFunction.apply(new OnStateBuilder<>(this, state));
            return this;
        }

        public StateMachine<STATE> build() {
            return new StateMachine<>(initialState, transitions, onEntryActions, onExitActions);
        }


        private <EVENT> void addTransition(STATE fromState, Class eventClass, Predicate<EVENT> condition, STATE toState, Consumer<EVENT> action) {
            transitions.add(new Transition<>(fromState, eventClass, condition, toState, action));
        }

        private void addOnStateEntryAction(STATE state, ArgumentlessConsumer action) {
            onEntryActions.put(state, action);
        }

        private void addOnStateExitAction(STATE state, ArgumentlessConsumer action) {
            onExitActions.put(state, action);
        }

        public static class OnStateBuilder<STATE> {
            private final Builder<STATE> builder;
            private final STATE state;
            private Class eventClass;


            private OnStateBuilder(Builder<STATE> builder, STATE state) {
                this.builder = builder;
                this.state = state;
            }

            public <EVENT> OnStateEventBuilder<STATE, EVENT> onEvent(Class<EVENT> eventClass) {
                this.eventClass = eventClass;
                return new OnStateEventBuilder<>(this);
            }

            public OnStateBuilder<STATE> onExitAction(ArgumentlessConsumer exitAction) {
                builder.addOnStateExitAction(state, exitAction);
                return this;
            }

            public OnStateBuilder<STATE> onExitConsumer(ArgumentlessConsumer exitConsumer) {
                return onExitAction(exitConsumer);
            }

            public OnStateBuilder<STATE> onEntryAction(ArgumentlessConsumer entryAction) {
                builder.addOnStateEntryAction(state, entryAction);
                return this;
            }

            public OnStateBuilder<STATE> onEntryConsumer(ArgumentlessConsumer entryConsumer) {
                return onEntryAction(entryConsumer);
            }

            private <EVENT> void addAction(Predicate<EVENT> condition, STATE toState, Consumer<EVENT> action) {
                builder.addTransition(state, eventClass, condition, toState == null ? state : toState, action);
            }
        }

        public static class OnStateEventBuilder<STATE, EVENT> {
            private final OnStateBuilder<STATE> onStateBuilder;
            private STATE toState;
            private Predicate<EVENT> condition;


            private OnStateEventBuilder(OnStateBuilder<STATE> onStateBuilder) {
                this.onStateBuilder = onStateBuilder;
                this.condition = (e) -> true;
            }

            public OnStateEventBuilder<STATE, EVENT> condition(Predicate<EVENT> condition) {
                this.condition = condition;
                return this;
            }

            public OnStateEventBuilder<STATE, EVENT> changeTo(STATE toState) {
                this.toState = toState;
                return this;
            }

            public OnStateBuilder<STATE> action(Consumer<EVENT> action) {
                onStateBuilder.addAction(condition, toState, action);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE> noAction() {
                onStateBuilder.addAction(condition, toState, (e) -> {
                });
                return onStateBuilder;
            }

            public OnStateBuilder<STATE> consumer(Consumer<EVENT> consumer) {
                onStateBuilder.addAction(condition, toState, consumer);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE> noConsumer() {
                onStateBuilder.addAction(condition, toState, (e) -> {
                });
                return onStateBuilder;
            }
        }
    }
}

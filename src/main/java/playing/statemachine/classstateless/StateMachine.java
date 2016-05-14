package playing.statemachine.classstateless;

import playing.statemachine.StateMachineTransition;
import playing.statemachine.StateMachineWriter;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StateMachine<STATE> implements StateMachineWriter<STATE, String> {
    private static final StatelessAction STATELESS_ACTION = () -> {
    };

    private STATE initialState;
    private List<Transition<STATE>> transitions;
    private Map<STATE, StatelessAction> onEntryActions;
    private Map<STATE, StatelessAction> onExitActions;

    private StateMachine(STATE initialState, List<Transition<STATE>> transitions, Map<STATE, StatelessAction> onEntryActions, Map<STATE, StatelessAction> onExitActions) {
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
                final StatelessAction onExitAction = onExitActions.getOrDefault(transition.fromState(), STATELESS_ACTION);
                final StatelessAction onEntryAction = onEntryActions.getOrDefault(transition.toState(), STATELESS_ACTION);
                final EventStatelessAction<Object> eventStatelessAction = (EventStatelessAction<Object>) transition.action;

                onExitAction.accept();
                eventStatelessAction.apply(event);
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
        private final Map<STATE, StatelessAction> onEntryActions = new HashMap<>();
        private final Map<STATE, StatelessAction> onExitActions = new HashMap<>();


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

        private <EVENT> void addTransition(STATE fromState, Class eventClass, Predicate<EVENT> condition, STATE toState) {
            addTransition(fromState, eventClass, condition, toState, (event) -> {
            });
        }

        private <EVENT> void addTransition(STATE fromState, Class eventClass, Predicate<EVENT> condition, STATE toState, EventStatelessAction<EVENT> action) {
            transitions.add(new Transition<>(fromState, eventClass, condition, toState, action));
        }

        private <EVENT> void addTransitionConsumer(STATE state, Class eventClass, Predicate<EVENT> condition, STATE toState, StatelessAction consumer) {
            addTransition(state, eventClass, condition, toState, (event) -> {
                consumer.accept();
            });
        }

        private void addOnStateEntryAction(STATE state, StatelessAction action) {
            onEntryActions.put(state, action);
        }

        private void addOnStateEntryConsumer(STATE state, StatelessAction action) {
            onEntryActions.put(state, STATELESS_ACTION);
        }

        private void addOnStateExitAction(STATE state, StatelessAction action) {
            onExitActions.put(state, action);
        }

        private void addOnStateExitConsumer(STATE state, StatelessAction action) {
            onExitActions.put(state, action::accept);
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

            public OnStateBuilder<STATE> onExitAction(StatelessAction exitAction) {
                builder.addOnStateExitAction(state, exitAction);
                return this;
            }

            public OnStateBuilder<STATE> onEntryAction(StatelessAction entryAction) {
                builder.addOnStateEntryAction(state, entryAction);
                return this;
            }

            private <EVENT> void addAction(Predicate<EVENT> condition, STATE toState, EventStatelessAction<EVENT> action) {
                builder.addTransition(state, eventClass, condition, toState == null ? state : toState, action);
            }

            private <EVENT> void addConsumer(Predicate<EVENT> condition, STATE toState, StatelessAction consumer) {
                builder.addTransitionConsumer(state, eventClass, condition, toState == null ? state : toState, consumer);
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

            public OnStateBuilder<STATE> action(EventStatelessAction<EVENT> action) {
                onStateBuilder.addAction(condition, toState, action);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE> noAction() {
                onStateBuilder.addAction(condition, toState, (e) -> {
                });
                return onStateBuilder;
            }

            public OnStateBuilder<STATE> consumer(StatelessAction consumer) {
                onStateBuilder.addConsumer(condition, toState, consumer);
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

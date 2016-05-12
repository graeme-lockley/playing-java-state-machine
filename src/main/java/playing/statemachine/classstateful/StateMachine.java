package playing.statemachine.classstateful;

import playing.util.Tuple;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class StateMachine<STATE, RS> {
    private final STATE initialState;
    private final List<Transition<STATE, RS>> transitions;
    private final Map<STATE, Action<RS>> onEntryActions;
    private final Map<STATE, Action<RS>> onExitActions;

    private final Action<RS> IDENTITY = x -> x;

    private StateMachine(STATE initialState, List<Transition<STATE, RS>> transitions, Map<STATE, Action<RS>> onEntryActions, Map<STATE, Action<RS>> onExitActions) {
        this.initialState = initialState;
        this.transitions = transitions;
        this.onEntryActions = onEntryActions;
        this.onExitActions = onExitActions;
    }

    public Tuple<STATE, RS> start(RS runtimeState) {
        return new Tuple<>(initialState, runtimeState);
    }

    public Tuple<STATE, RS> process(Tuple<STATE, RS> machineState, Object... events) {
        Tuple<STATE, RS> runningMachineState = machineState;

        for (Object event : events) {
            final Optional<Transition<STATE, RS>> transitionOptional = findTransition(runningMachineState._1, event);

            if (transitionOptional.isPresent()) {
                final Transition<STATE, RS> transition = transitionOptional.get();
                final Action<RS> onExitAction = onExitActions.getOrDefault(transition.fromState, IDENTITY);
                final Action<RS> onEntryAction = onEntryActions.getOrDefault(transition.toState, IDENTITY);
                final EventAction<Object, RS> eventAction = (EventAction<Object, RS>) transition.action;

                runningMachineState = new Tuple<>(transition.toState,
                        onEntryAction.apply(
                                eventAction.apply(
                                        onExitAction.apply(runningMachineState._2),
                                        event)));
            }
        }

        return runningMachineState;
    }

    private Optional<Transition<STATE, RS>> findTransition(STATE state, Object event) {
        return transitions.stream().filter(t -> t.fromState == state && t.eventClass == event.getClass()).findFirst();
    }

    public static class Builder<STATE, RS> {
        private STATE initialState;
        private final List<Transition<STATE, RS>> transitions = new ArrayList<>();
        private final Map<STATE, Action<RS>> onEntryActions = new HashMap<>();
        private final Map<STATE, Action<RS>> onExitActions = new HashMap<>();

        public Builder<STATE, RS> initialState(STATE initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder<STATE, RS> onState(STATE state, Function<OnStateBuilder<STATE, RS>, OnStateBuilder<STATE, RS>> onStateBuilderFunction) {
            onStateBuilderFunction.apply(new OnStateBuilder<>(this, state));
            return this;
        }

        public StateMachine<STATE, RS> build() {
            return new StateMachine<>(initialState, transitions, onEntryActions, onExitActions);
        }

        private void addTransition(STATE fromState, Class eventClass, STATE toState) {
            addTransition(fromState, eventClass, toState, (fromRuntimeState, event) -> fromRuntimeState);
        }

        private <EVENT> void addTransition(STATE fromState, Class eventClass, STATE toState, EventAction<EVENT, RS> action) {
            transitions.add(new Transition<>(fromState, eventClass, toState, action));
        }

        private void addTransitionConsumer(STATE state, Class eventClass, STATE toState, Consumer<RS> consumer) {
            addTransition(state, eventClass, toState, (fromRuntimeState, event) -> {
                consumer.accept(fromRuntimeState);
                return fromRuntimeState;
            });
        }

        private void addOnStateEntryAction(STATE state, Action<RS> action) {
            onEntryActions.put(state, action);
        }

        private void addOnStateEntryConsumer(STATE state, Consumer<RS> action) {
            onEntryActions.put(state, runtimeState -> {
                action.accept(runtimeState);
                return runtimeState;
            });
        }

        private void addOnStateExitAction(STATE state, Action<RS> action) {
            onExitActions.put(state, action);
        }

        private void addOnStateExitConsumer(STATE state, Consumer<RS> action) {
            onExitActions.put(state, runtimeState -> {
                action.accept(runtimeState);
                return runtimeState;
            });
        }

        public static class OnStateBuilder<STATE, RS> {
            private final Builder<STATE, RS> builder;
            private final STATE state;
            private Class eventClass;


            private OnStateBuilder(Builder<STATE, RS> builder, STATE state) {
                this.builder = builder;
                this.state = state;
            }

            public <EVENT> OnStateEventBuilder<STATE, EVENT, RS> onEvent(Class<EVENT> eventClass) {
                this.eventClass = eventClass;
                return new OnStateEventBuilder<>(this);
            }

            public OnStateBuilder<STATE, RS> onExitAction(Action<RS> exitAction) {
                builder.addOnStateExitAction(state, exitAction);
                return this;
            }

            public OnStateBuilder<STATE, RS> onEntryAction(Action<RS> entryAction) {
                builder.addOnStateEntryAction(state, entryAction);
                return this;
            }

            private <EVENT> void addAction(STATE toState, EventAction<EVENT, RS> action) {
                builder.addTransition(state, eventClass, toState == null ? state : toState, action);
            }

            private void addConsumer(STATE toState, Consumer<RS> consumer) {
                builder.addTransitionConsumer(state, eventClass, toState == null ? state : toState, consumer);
            }
        }

        public static class OnStateEventBuilder<STATE, EVENT, RS> {
            private final OnStateBuilder<STATE, RS> onStateBuilder;
            private STATE toState;


            private OnStateEventBuilder(OnStateBuilder<STATE, RS> onStateBuilder) {
                this.onStateBuilder = onStateBuilder;
            }

            public OnStateEventBuilder<STATE, EVENT, RS> changeTo(STATE toState) {
                this.toState = toState;
                return this;
            }

            public OnStateBuilder<STATE, RS> action(EventAction<EVENT, RS> action) {
                onStateBuilder.addAction(toState, action);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, RS> noAction() {
                onStateBuilder.addAction(toState, (x, e) -> x);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, RS> consumer(Consumer<RS> consumer) {
                onStateBuilder.addConsumer(toState, consumer);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, RS> noConsumer() {
                onStateBuilder.addAction(toState, (x, e) -> x);
                return onStateBuilder;
            }
        }
    }
}

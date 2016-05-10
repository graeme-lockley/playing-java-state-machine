package playing.statemachine.nativestateful;

import playing.util.Tuple;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class StateMachine<STATE, EVENT, RS> {
    private final STATE initialState;
    private final List<Transition<STATE, EVENT, RS>> transitions;
    private final Map<STATE, Action<RS>> onEntryActions;
    private final Map<STATE, Action<RS>> onExitActions;

    private final Action<RS> IDENTITY = x -> x;

    private StateMachine(STATE initialState, List<Transition<STATE, EVENT, RS>> transitions, Map<STATE, Action<RS>> onEntryActions, Map<STATE, Action<RS>> onExitActions) {
        this.initialState = initialState;
        this.transitions = transitions;
        this.onEntryActions = onEntryActions;
        this.onExitActions = onExitActions;
    }

    public Tuple<STATE, RS> start(RS runtimeState) {
        return new Tuple<>(initialState, runtimeState);
    }

    public Tuple<STATE, RS> process(Tuple<STATE, RS> machineState, EVENT... events) {
        Tuple<STATE, RS> runningMachineState = machineState;

        for (EVENT event : events) {
            final Optional<Transition<STATE, EVENT, RS>> transitionOptional = findTransition(runningMachineState._1, event);

            if (transitionOptional.isPresent()) {
                final Transition<STATE, EVENT, RS> transition = transitionOptional.get();
                final Action<RS> onExitAction = onExitActions.getOrDefault(transition.fromState, IDENTITY);
                final Action<RS> onEntryAction = onEntryActions.getOrDefault(transition.toState, IDENTITY);

                runningMachineState = new Tuple<>(transition.toState,
                        onEntryAction.apply(
                                transition.action.apply(
                                        onExitAction.apply(runningMachineState._2))));
            }
        }

        return runningMachineState;
    }

    private Optional<Transition<STATE, EVENT, RS>> findTransition(STATE state, EVENT event) {
        return transitions.stream().filter(t -> t.fromState == state && t.event == event).findFirst();
    }

    public static class Builder<STATE, EVENT, RS> {
        private STATE initialState;
        private final List<Transition<STATE, EVENT, RS>> transitions = new ArrayList<>();
        private final Map<STATE, Action<RS>> onEntryActions = new HashMap<>();
        private final Map<STATE, Action<RS>> onExitActions = new HashMap<>();

        public Builder<STATE, EVENT, RS> initialState(STATE initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder<STATE, EVENT, RS> onState(STATE state, Function<OnStateBuilder<STATE, EVENT, RS>, OnStateBuilder<STATE, EVENT, RS>> onStateBuilderFunction) {
            OnStateBuilder<STATE, EVENT, RS> onStateBuilder = onStateBuilderFunction.apply(new OnStateBuilder<>(this, state));
            return this;
        }

        public StateMachine<STATE, EVENT, RS> build() {
            return new StateMachine<>(initialState, transitions, onEntryActions, onExitActions);
        }

        private void addTransition(STATE fromState, EVENT event, STATE toState) {
            addTransition(fromState, event, toState, (fromRuntimeState) -> fromRuntimeState);
        }

        private void addTransition(STATE fromState, EVENT event, STATE toState, Action<RS> action) {
            transitions.add(new Transition<>(fromState, event, toState, action));
        }

        private void addTransitionConsumer(STATE state, EVENT event, STATE toState, Consumer<RS> consumer) {
            addTransition(state, event, toState, (fromRuntimeState) -> {
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

        public static class OnStateBuilder<STATE, EVENT, RS> {
            private final Builder<STATE, EVENT, RS> builder;
            private final STATE state;
            private EVENT event;


            private OnStateBuilder(Builder<STATE, EVENT, RS> builder, STATE state) {
                this.builder = builder;
                this.state = state;
            }

            public OnStateEventBuilder<STATE, EVENT, RS> onEvent(EVENT event) {
                this.event = event;
                return new OnStateEventBuilder<>(this);
            }

            public OnStateBuilder<STATE, EVENT, RS> onExitAction(Action<RS> exitAction) {
                builder.addOnStateExitAction(state, exitAction);
                return this;
            }

            public OnStateBuilder<STATE, EVENT, RS> onEntryAction(Action<RS> entryAction) {
                builder.addOnStateEntryAction(state, entryAction);
                return this;
            }

            private void addAction(STATE toState, Action<RS> action) {
                builder.addTransition(state, event, toState == null ? state : toState, action);
            }

            private void addConsumer(STATE toState, Consumer<RS> consumer) {
                builder.addTransitionConsumer(state, event, toState == null ? state : toState, consumer);
            }
        }

        public static class OnStateEventBuilder<STATE, EVENT, RS> {
            private final OnStateBuilder<STATE, EVENT, RS> onStateBuilder;
            private STATE toState;


            private OnStateEventBuilder(OnStateBuilder<STATE, EVENT, RS> onStateBuilder) {
                this.onStateBuilder = onStateBuilder;
            }

            public OnStateEventBuilder<STATE, EVENT, RS> changeTo(STATE toState) {
                this.toState = toState;
                return this;
            }

            public OnStateBuilder<STATE, EVENT, RS> action(Action<RS> action) {
                onStateBuilder.addAction(toState, action);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT, RS> noAction() {
                onStateBuilder.addAction(toState, (x) -> x);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT, RS> consumer(Consumer<RS> consumer) {
                onStateBuilder.addConsumer(toState, consumer);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT, RS> noConsumer() {
                onStateBuilder.addAction(toState, (x) -> x);
                return onStateBuilder;
            }
        }
    }
}

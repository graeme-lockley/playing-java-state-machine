package playing.statemachine.nativestateful;

import playing.statemachine.StateMachineTransition;
import playing.statemachine.StateMachineWriter;
import playing.util.Tuple;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StateMachine<STATE, EVENT, RS> implements StateMachineWriter<STATE, EVENT> {
    private final STATE initialState;
    private final List<Transition<STATE, EVENT, RS>> transitions;
    private final Map<STATE, Function<RS, RS>> onEntryActions;
    private final Map<STATE, Function<RS, RS>> onExitActions;

    private final Function<RS, RS> IDENTITY = x -> x;

    private StateMachine(STATE initialState, List<Transition<STATE, EVENT, RS>> transitions, Map<STATE, Function<RS, RS>> onEntryActions, Map<STATE, Function<RS, RS>> onExitActions) {
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
            final Optional<Transition<STATE, EVENT, RS>> transitionOptional = findTransition(runningMachineState._1, runningMachineState._2, event);

            if (transitionOptional.isPresent()) {
                final Transition<STATE, EVENT, RS> transition = transitionOptional.get();
                final Function<RS, RS> onExitAction = onExitActions.getOrDefault(transition.fromState(), IDENTITY);
                final Function<RS, RS> onEntryAction = onEntryActions.getOrDefault(transition.toState(), IDENTITY);

                runningMachineState = new Tuple<>(transition.toState(),
                        onEntryAction.apply(
                                transition.actionApply(
                                        onExitAction.apply(runningMachineState._2))));
            }
        }

        return runningMachineState;
    }

    private Optional<Transition<STATE, EVENT, RS>> findTransition(STATE state, RS runtimeState, EVENT event) {
        return transitions.stream().filter(t -> t.canFire(state, runtimeState, event)).findFirst();
    }

    @Override
    public STATE initialState() {
        return initialState;
    }

    @Override
    public Collection<StateMachineTransition<STATE, EVENT>> transitions() {
        return transitions.stream().collect(Collectors.toList());
    }

    public static class Builder<STATE, EVENT, RS> {
        private STATE initialState;
        private final List<Transition<STATE, EVENT, RS>> transitions = new ArrayList<>();
        private final Map<STATE, Function<RS, RS>> onEntryActions = new HashMap<>();
        private final Map<STATE, Function<RS, RS>> onExitActions = new HashMap<>();

        public Builder<STATE, EVENT, RS> initialState(STATE initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder<STATE, EVENT, RS> onState(STATE state, Function<OnStateBuilder<STATE, EVENT, RS>, OnStateBuilder<STATE, EVENT, RS>> onStateBuilderFunction) {
            onStateBuilderFunction.apply(new OnStateBuilder<>(this, state));
            return this;
        }

        public StateMachine<STATE, EVENT, RS> build() {
            return new StateMachine<>(initialState, transitions, onEntryActions, onExitActions);
        }

        private void addTransition(STATE fromState, EVENT event, BiPredicate<RS, EVENT> condition, STATE toState, Function<RS, RS> action) {
            transitions.add(new Transition<>(fromState, event, condition, toState, action));
        }

        private void addTransitionConsumer(STATE state, EVENT event, BiPredicate<RS, EVENT> condition, STATE toState, Consumer<RS> consumer) {
            addTransition(state, event, condition, toState, (fromRuntimeState) -> {
                consumer.accept(fromRuntimeState);
                return fromRuntimeState;
            });
        }

        private void addOnStateEntryAction(STATE state, Function<RS, RS> action) {
            onEntryActions.put(state, action);
        }

        private void addOnStateExitAction(STATE state, Function<RS, RS> action) {
            onExitActions.put(state, action);
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

            public OnStateBuilder<STATE, EVENT, RS> onEntryAction(Function<RS, RS> entryAction) {
                builder.addOnStateEntryAction(state, entryAction);
                return this;
            }

            public OnStateBuilder<STATE, EVENT, RS> onEntryConsumer(Consumer<RS> entryAction) {
                builder.addOnStateEntryAction(state, (runtimeState) -> {
                    entryAction.accept(runtimeState);
                    return runtimeState;
                });
                return this;
            }

            public OnStateBuilder<STATE, EVENT, RS> onExitAction(Function<RS, RS> exitAction) {
                builder.addOnStateExitAction(state, exitAction);
                return this;
            }

            public OnStateBuilder<STATE, EVENT, RS> onExitConsumer(Function<RS, RS> exitAction) {
                builder.addOnStateExitAction(state, (runtimeState -> {
                    exitAction.apply(runtimeState);
                    return runtimeState;
                }));
                return this;
            }

            private void addAction(BiPredicate<RS, EVENT> condition, STATE toState, Function<RS, RS> action) {
                builder.addTransition(state, event, condition, toState == null ? state : toState, action);
            }

            private void addConsumer(BiPredicate<RS, EVENT> condition, STATE toState, Consumer<RS> consumer) {
                builder.addTransitionConsumer(state, event, condition, toState == null ? state : toState, consumer);
            }
        }

        public static class OnStateEventBuilder<STATE, EVENT, RS> {
            private final OnStateBuilder<STATE, EVENT, RS> onStateBuilder;
            private STATE toState;
            private BiPredicate<RS, EVENT> condition = (s, e) -> true;


            private OnStateEventBuilder(OnStateBuilder<STATE, EVENT, RS> onStateBuilder) {
                this.onStateBuilder = onStateBuilder;
            }

            public OnStateEventBuilder<STATE, EVENT, RS> changeTo(STATE toState) {
                this.toState = toState;
                return this;
            }

            public OnStateEventBuilder<STATE, EVENT, RS> condition(BiPredicate<RS, EVENT> condition) {
                this.condition = condition;
                return this;
            }

            public OnStateBuilder<STATE, EVENT, RS> action(Function<RS, RS> action) {
                onStateBuilder.addAction(condition, toState, action);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT, RS> noAction() {
                onStateBuilder.addAction(condition, toState, (x) -> x);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT, RS> consumer(Consumer<RS> consumer) {
                onStateBuilder.addConsumer(condition, toState, consumer);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT, RS> noConsumer() {
                onStateBuilder.addAction(condition, toState, (x) -> x);
                return onStateBuilder;
            }
        }
    }
}

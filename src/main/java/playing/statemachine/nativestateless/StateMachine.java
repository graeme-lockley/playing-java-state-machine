package playing.statemachine.nativestateless;

import playing.statemachine.StateMachineTransition;
import playing.statemachine.StateMachineWriter;
import playing.util.VoidConsumer;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StateMachine<STATE, EVENT> implements StateMachineWriter<STATE, EVENT> {
    private final STATE initialState;
    private final List<Transition<STATE, EVENT>> transitions;
    private final Map<STATE, VoidConsumer> onEntryActions;
    private final Map<STATE, VoidConsumer> onExitActions;

    private final static VoidConsumer IDENTITY = () -> {
    };

    private StateMachine(STATE initialState, List<Transition<STATE, EVENT>> transitions, Map<STATE, VoidConsumer> onEntryActions, Map<STATE, VoidConsumer> onExitActions) {
        this.initialState = initialState;
        this.transitions = transitions;
        this.onEntryActions = onEntryActions;
        this.onExitActions = onExitActions;
    }

    public STATE start() {
        return initialState;
    }

    public STATE process(STATE machineState, EVENT... events) {
        STATE runningMachineState = machineState;

        for (EVENT event : events) {
            final Optional<Transition<STATE, EVENT>> transitionOptional = findTransition(runningMachineState, event);

            if (transitionOptional.isPresent()) {
                final Transition<STATE, EVENT> transition = transitionOptional.get();
                final VoidConsumer onExitAction = onExitActions.getOrDefault(transition.fromState(), IDENTITY);
                final VoidConsumer onEntryAction = onEntryActions.getOrDefault(transition.toState(), IDENTITY);

                onExitAction.accept();
                transition.actionAccept();
                onEntryAction.accept();

                runningMachineState = transition.toState();
            }
        }

        return runningMachineState;
    }

    private Optional<Transition<STATE, EVENT>> findTransition(STATE state, EVENT event) {
        return transitions.stream().filter(t -> t.canFire(state, event)).findFirst();
    }

    @Override
    public STATE initialState() {
        return initialState;
    }

    @Override
    public Collection<StateMachineTransition<STATE, EVENT>> transitions() {
        return transitions.stream().collect(Collectors.toList());
    }

    public static class Builder<STATE, EVENT> {
        private STATE initialState;
        private final List<Transition<STATE, EVENT>> transitions = new ArrayList<>();
        private final Map<STATE, VoidConsumer> onEntryActions = new HashMap<>();
        private final Map<STATE, VoidConsumer> onExitActions = new HashMap<>();

        public Builder<STATE, EVENT> initialState(STATE initialState) {
            this.initialState = initialState;
            return this;
        }

        public Builder<STATE, EVENT> onState(STATE state, Function<OnStateBuilder<STATE, EVENT>, OnStateBuilder<STATE, EVENT>> onStateBuilderFunction) {
            onStateBuilderFunction.apply(new OnStateBuilder<>(this, state));
            return this;
        }

        public StateMachine<STATE, EVENT> build() {
            return new StateMachine<>(initialState, transitions, onEntryActions, onExitActions);
        }

        private void addTransition(STATE fromState, EVENT event, STATE toState, VoidConsumer action) {
            transitions.add(new Transition<>(fromState, event, toState, action));
        }

        private void addOnStateEntryAction(STATE state, VoidConsumer action) {
            onEntryActions.put(state, action);
        }


        private void addOnStateExitAction(STATE state, VoidConsumer action) {
            onExitActions.put(state, action);
        }

        public static class OnStateBuilder<STATE, EVENT> {
            private final Builder<STATE, EVENT> builder;
            private final STATE state;
            private EVENT event;


            private OnStateBuilder(Builder<STATE, EVENT> builder, STATE state) {
                this.builder = builder;
                this.state = state;
            }

            public OnStateEventBuilder<STATE, EVENT> onEvent(EVENT event) {
                this.event = event;
                return new OnStateEventBuilder<>(this);
            }

            public OnStateBuilder<STATE, EVENT> onExitAction(VoidConsumer exitAction) {
                builder.addOnStateExitAction(state, exitAction);
                return this;
            }

            public OnStateBuilder<STATE, EVENT> onExitConsumer(VoidConsumer exitConsumer) {
                builder.addOnStateExitAction(state, exitConsumer);
                return this;
            }

            public OnStateBuilder<STATE, EVENT> onEntryAction(VoidConsumer entryAction) {
                builder.addOnStateEntryAction(state, entryAction);
                return this;
            }

            public OnStateBuilder<STATE, EVENT> onEntryConsumer(VoidConsumer entryConsumer) {
                return onEntryAction(entryConsumer);
            }

            private void addAction(STATE toState, VoidConsumer action) {
                builder.addTransition(state, event, toState == null ? state : toState, action);
            }
        }

        public static class OnStateEventBuilder<STATE, EVENT> {
            private final OnStateBuilder<STATE, EVENT> onStateBuilder;
            private STATE toState;


            private OnStateEventBuilder(OnStateBuilder<STATE, EVENT> onStateBuilder) {
                this.onStateBuilder = onStateBuilder;
            }

            public OnStateEventBuilder<STATE, EVENT> changeTo(STATE toState) {
                this.toState = toState;
                return this;
            }

            public OnStateBuilder<STATE, EVENT> action(VoidConsumer action) {
                onStateBuilder.addAction(toState, action);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT> noAction() {
                onStateBuilder.addAction(toState, IDENTITY);
                return onStateBuilder;
            }

            public OnStateBuilder<STATE, EVENT> consumer(VoidConsumer consumer) {
                return action(consumer);
            }

            public OnStateBuilder<STATE, EVENT> noConsumer() {
                return noAction();
            }
        }
    }
}

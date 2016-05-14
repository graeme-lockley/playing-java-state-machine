package playing.statemachine.classstateless;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClassStatelessTest {
    private StateMachine<Integer> stateMachine;
    private List<String> trace = new ArrayList<>();

    @Before
    public void before() {
        stateMachine = new StateMachine.Builder<Integer>()
                .initialState(1)
                .onState(1, s -> s
                        .onEntryConsumer(() -> addTrace("entryConsumer: 1"))
                        .onEvent(Integer.class).condition(event -> event < 100).action(event -> addTrace(1, 1, event))
                        .onEvent(Integer.class).condition(event -> event >= 100).changeTo(2).action(event -> addTrace(1, 2, event))
                        .onExitConsumer(() -> addTrace("exitConsumer: 1")))
                .onState(2, s -> s
                        .onEntryAction(() -> addTrace("entryAction: 2"))
                        .onEvent(Integer.class).changeTo(3).noAction()
                        .onExitAction(() -> addTrace("exitAction: 2")))
                .onState(3, s -> s
                        .onEntryConsumer(() -> addTrace("entryConsumer: 3"))
                        .onEvent(Integer.class).changeTo(4).consumer(event -> addTrace(3, 4, event))
                        .onExitConsumer(() -> addTrace("exitConsumer: 3")))
                .onState(4, s -> s
                        .onEntryConsumer(() -> addTrace("entryConsumer: 4"))
                        .onEvent(Integer.class).changeTo(5).noConsumer()
                        .onExitConsumer(() -> addTrace("exitConsumer: 4")))
                .build();
    }

    private void addTrace(int fromState, int toState, int event) {
        trace.add("transition: " + fromState + " to " + toState + ": E=" + event);
    }

    private void addTrace(String traceEntry) {
        trace.add(traceEntry);
    }

    @Test
    public void given_events_should_show_the_desired_trace() throws Exception {
        final int process = stateMachine.process(stateMachine.initialState(), 1, 101, 4, 5, 6);

        assertEquals(5, (int) process);

        assertEquals(Arrays.asList(
                "exitConsumer: 1",
                "transition: 1 to 1: E=1",
                "entryConsumer: 1",
                "exitConsumer: 1",
                "transition: 1 to 2: E=101",
                "entryAction: 2",
                "exitAction: 2",
                "entryConsumer: 3",
                "exitConsumer: 3",
                "transition: 3 to 4: E=5",
                "entryConsumer: 4",
                "exitConsumer: 4"
        ), trace);
    }
}

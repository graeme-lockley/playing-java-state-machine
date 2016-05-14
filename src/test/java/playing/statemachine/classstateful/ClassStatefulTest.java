package playing.statemachine.classstateful;

import org.junit.Before;
import org.junit.Test;
import playing.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClassStatefulTest {
    private StateMachine<Integer, Integer> stateMachine;
    private List<String> trace = new ArrayList<>();

    @Before
    public void before() {
        stateMachine = new StateMachine.Builder<Integer, Integer>()
                .initialState(1)
                .onState(1, s -> s
                        .onEntryConsumer((state) -> addTrace(state, "entryConsumer: 1"))
                        .onEvent(Integer.class).condition((state, event) -> event < 100).action((state, event) -> addTrace(1, 1, state, event))
                        .onEvent(Integer.class).condition((state, event) -> event >= 100).changeTo(2).action((state, event) -> addTrace(1, 2, state, event))
                        .onExitConsumer((state) -> addTrace(state, "exitConsumer: 1")))
                .onState(2, s -> s
                        .onEntryAction((state) -> addTrace(state, "entryAction: 2"))
                        .onEvent(Integer.class).changeTo(3).noAction()
                        .onExitAction(state -> addTrace(state, "exitAction: 2")))
                .onState(3, s -> s
                        .onEntryConsumer((state) -> addTrace(state, "entryConsumer: 3"))
                        .onEvent(Integer.class).changeTo(4).consumer((state, event) -> addTrace(3, 4, state, event))
                        .onExitConsumer(state -> addTrace(state, "exitConsumer: 3")))
                .onState(4, s -> s
                        .onEntryConsumer((state) -> addTrace(state, "entryConsumer: 4"))
                        .onEvent(Integer.class).changeTo(5).noConsumer()
                        .onExitConsumer(state -> addTrace(state, "exitConsumer: 4")))
                .build();
    }

    private int addTrace(int fromState, int toState, int runtimeState, int event) {
        trace.add("transition: " + fromState + " to " + toState + ": RS=" + runtimeState + ": E=" + event);
        return runtimeState + 1;
    }

    private int addTrace(int runtimeState, String traceEntry) {
        trace.add(traceEntry + ": RS=" + runtimeState);
        return runtimeState + 1;
    }

    @Test
    public void given_events_should_show_the_desired_trace() throws Exception {
        final Tuple<Integer, Integer> process = stateMachine.process(new Tuple<>(stateMachine.initialState(), 0), 1, 101, 4, 5, 6);

        assertEquals(5, (int) process._1);
        assertEquals(4, (int) process._2);

        assertEquals(Arrays.asList(
                "exitConsumer: 1: RS=0",
                "transition: 1 to 1: RS=0: E=1",
                "entryConsumer: 1: RS=1",
                "exitConsumer: 1: RS=1",
                "transition: 1 to 2: RS=1: E=101",
                "entryAction: 2: RS=2",
                "exitAction: 2: RS=3",
                "entryConsumer: 3: RS=4",
                "exitConsumer: 3: RS=4",
                "transition: 3 to 4: RS=4: E=5",
                "entryConsumer: 4: RS=4",
                "exitConsumer: 4: RS=4"
        ), trace);
    }
}

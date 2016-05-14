package playing.statemachine.nativestateful;

import org.junit.Before;
import org.junit.Test;
import playing.statemachine.PlantUMLWriter;
import playing.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NativeStatefulTest {
    private StateMachine<Integer, Integer, Integer> stateMachine;
    private List<String> trace = new ArrayList<>();

    @Before
    public void before() {
        stateMachine = new StateMachine.Builder<Integer, Integer, Integer>()
                .initialState(1)
                .onState(1, s -> s
                        .onEntryConsumer((state) -> addTrace(state, "entryConsumer: 1"))
                        .onEvent(1).condition((state, event) -> event < 100).action(state -> addTrace(1, 1, state, 1))
                        .onEvent(101).condition((state, event) -> event < 100).action(state -> addTrace(1, 1, state, 101))
                        .onEvent(100).condition((state, event) -> event >= 100).changeTo(2).action(state -> addTrace(1, 2, state, 100))
                        .onExitConsumer((state) -> addTrace(state, "exitConsumer: 1")))
                .onState(2, s -> s
                        .onEntryAction((state) -> addTrace(state, "entryAction: 2"))
                        .onEvent(200).changeTo(3).noAction()
                        .onExitAction(state -> addTrace(state, "exitAction: 2")))
                .onState(3, s -> s
                        .onEntryConsumer((state) -> addTrace(state, "entryConsumer: 3"))
                        .onEvent(300).changeTo(4).consumer(state -> addTrace(3, 4, state, 300))
                        .onExitConsumer(state -> addTrace(state, "exitConsumer: 3")))
                .onState(4, s -> s
                        .onEntryConsumer((state) -> addTrace(state, "entryConsumer: 4"))
                        .onEvent(400).changeTo(5).noConsumer()
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
    public void should_write_out_the_PlantUML_statemachine_description() throws Exception {
        PlantUMLWriter.write(stateMachine, "target/nativestateful_test.puml", 600);
    }

    @Test
    public void given_events_should_show_the_desired_trace() throws Exception {
        final Tuple<Integer, Integer> process = stateMachine.process(new Tuple<>(stateMachine.initialState(), 0), 1, 100, 200, 300, 400);

        assertEquals(5, (int) process._1);
        assertEquals(4, (int) process._2);

        assertEquals(Arrays.asList(
                "exitConsumer: 1: RS=0",
                "transition: 1 to 1: RS=0: E=1",
                "entryConsumer: 1: RS=1",
                "exitConsumer: 1: RS=1",
                "transition: 1 to 2: RS=1: E=100",
                "entryAction: 2: RS=2",
                "exitAction: 2: RS=3",
                "entryConsumer: 3: RS=4",
                "exitConsumer: 3: RS=4",
                "transition: 3 to 4: RS=4: E=300",
                "entryConsumer: 4: RS=4",
                "exitConsumer: 4: RS=4"
        ), trace);
    }
}

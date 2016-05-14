package playing.statemachine.nativestateless.coincount;

import org.junit.Before;
import org.junit.Test;
import playing.statemachine.nativestateless.StateMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static playing.statemachine.nativestateless.coincount.Event.*;
import static playing.statemachine.nativestateless.coincount.State.*;

public class CoinCountTest {
    private StateMachine<State, Event> stateMachine;
    private List<String> trace = new ArrayList<>();

    @Before
    public void before() {
        stateMachine = new StateMachine.Builder<State, Event>()
                .initialState(INITIAL)
                .onState(INITIAL, s -> s
                        .onEvent(MIRACLE).changeTo(COUNT).consumer(() -> trace.add("Event: miracle"))
                        .onExitAction(() -> trace.add("ExitAction: INITIAL")))
                .onState(COUNT, s -> s
                        .onEntryAction(() -> trace.add("EntryAction: COUNT"))
                        .onExitAction(() -> trace.add("ExitAction: COUNT"))
                        .onEvent(HEADS).noAction()
                        .onEvent(TAILS).noConsumer()
                        .onEvent(MIRACLE).changeTo(FINAL).action(() -> trace.add("Event: miracle")))
                .onState(FINAL, s -> s
                        .onEntryAction(() -> trace.add("EntryAction: FINAL")))
                .build();
    }

    @Test
    public void when_in_INITIAL_state_given_a_MIRACLE_event_should_move_into_COUNT_state() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, MIRACLE);

        assertEquals(COUNT, process);
        assertEquals(Arrays.asList(
                "ExitAction: INITIAL",
                "Event: miracle",
                "EntryAction: COUNT"), trace);
    }

    @Test
    public void given_in_INITIAL_state_when_a_TAIL_event_should_remain_in_INITAL_state() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, TAILS);

        assertEquals(INITIAL, process);
        assertEquals(Collections.emptyList(), trace);
    }

    @Test
    public void when_in_INITIAL_state_given_two_MIRACLE_events_should_move_into_FINAL_state() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, MIRACLE, MIRACLE);

        assertEquals(FINAL, process);
        assertEquals(Arrays.asList(
                "ExitAction: INITIAL",
                "Event: miracle",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "Event: miracle",
                "EntryAction: FINAL"), trace);
    }

    @Test
    public void when_in_INITIAL_state_given_events_MIRACLE_HEAD_HEAD_TAILS_MIRACLE_should_move_into_FINAL_state_with_correct_counts() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, MIRACLE, HEADS, HEADS, TAILS, MIRACLE);

        assertEquals(FINAL, process);
        assertEquals(Arrays.asList(
                "ExitAction: INITIAL",
                "Event: miracle",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "Event: miracle",
                "EntryAction: FINAL"), trace);
    }
}

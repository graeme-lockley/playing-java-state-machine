package playing.statemachine.classstateless.coincount;

import org.junit.Before;
import org.junit.Test;
import playing.statemachine.classstateless.StateMachine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static playing.statemachine.classstateless.coincount.State.*;

public class CoinCountTest {
    private StateMachine<State> stateMachine;
    private List<String> trace = new ArrayList<>();

    @Before
    public void before() {
        stateMachine = new StateMachine.Builder<State>()
                .initialState(INITIAL)
                .onState(INITIAL, s -> s
                        .onEvent(MiracleToss.class).changeTo(COUNT).action(e -> trace.add("Event: MiracleToss"))
                        .onExitAction(() -> trace.add("ExitAction: INITIAL")))
                .onState(COUNT, s -> s
                        .onEntryAction(() -> trace.add("EntryAction: COUNT"))
                        .onExitAction(() -> trace.add("ExitAction: COUNT"))
                        .onEvent(HeadsToss.class).action(e -> trace.add("Event: HeadsToss"))
                        .onEvent(TailsToss.class).action(e -> trace.add("Event: TailsToss"))
                        .onEvent(MiracleToss.class).changeTo(FINAL).action(e -> trace.add("Event: MiracleToss")))
                .onState(FINAL, s -> s
                        .onEntryAction(() -> trace.add("EntryAction: FINAL"))
                )
                .build();
    }

    @Test
    public void when_in_INITIAL_state_given_a_MIRACLE_event_should_move_into_COUNT_state() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, new MiracleToss(1));

        assertEquals(COUNT, process);
        assertEquals(Arrays.asList(
                "ExitAction: INITIAL",
                "Event: MiracleToss",
                "EntryAction: COUNT"), trace);
    }

    @Test
    public void given_in_INITIAL_state_when_a_TAIL_event_should_remain_in_INITAL_state() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, new TailsToss(2));

        assertEquals(INITIAL, process);
        assertEquals(Collections.emptyList(), trace);
    }

    @Test
    public void when_in_INITIAL_state_given_two_MIRACLE_events_should_move_into_FINAL_state() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, new MiracleToss(3), new MiracleToss(4));

        assertEquals(FINAL, process);
        assertEquals(Arrays.asList(
                "ExitAction: INITIAL",
                "Event: MiracleToss",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "Event: MiracleToss",
                "EntryAction: FINAL"), trace);
    }

    @Test
    public void when_in_INITIAL_state_given_events_MIRACLE_HEAD_HEAD_TAILS_MIRACLE_should_move_into_FINAL_state_with_correct_counts() throws Exception {
        final State startState = stateMachine.start();
        final State process = stateMachine.process(startState, new MiracleToss(5), new HeadsToss(6), new HeadsToss(7), new TailsToss(8), new MiracleToss(9));

        assertEquals(FINAL, process);
        assertEquals(Arrays.asList(
                "ExitAction: INITIAL",
                "Event: MiracleToss",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "Event: HeadsToss",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "Event: HeadsToss",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "Event: TailsToss",
                "EntryAction: COUNT",
                "ExitAction: COUNT",
                "Event: MiracleToss",
                "EntryAction: FINAL"), trace);
    }
}

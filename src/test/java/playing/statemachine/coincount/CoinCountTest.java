package playing.statemachine.coincount;

import org.junit.Before;
import org.junit.Test;
import playing.statemachine.StateMachine;
import playing.util.Tuple;

import static org.junit.Assert.assertEquals;
import static playing.statemachine.coincount.Event.*;
import static playing.statemachine.coincount.State.*;

public class CoinCountTest {
    private StateMachine<State, Event, RuntimeState> stateMachine;

    @Before
    public void before() {
        stateMachine = new StateMachine.StateMachineBuilder<State, Event, RuntimeState>()
                .initialState(INITIAL)
                .onState(INITIAL, s -> s
                        .onEvent(MIRACLE).changeTo(COUNT).noAction()
                        .onExitAction(RuntimeState::exitInitialState))
                .onState(COUNT, s -> s
                        .onEntryAction(RuntimeState::enterCountState)
                        .onEvent(HEADS).action(RuntimeState::incHeads)
                        .onEvent(TAILS).action(RuntimeState::incTails)
                        .onEvent(MIRACLE).changeTo(FINAL).noAction())
                .build();
    }

    @Test
    public void when_in_INITIAL_state_given_a_MIRACLE_event_should_move_into_COUNT_state() throws Exception {
        final Tuple<State, RuntimeState> startState = stateMachine.start(new RuntimeState());
        final Tuple<State, RuntimeState> process = stateMachine.process(startState, MIRACLE);

        assertEquals(COUNT, process._1);
        assertEquals(0, process._2.heads);
        assertEquals(0, process._2.tails);
        assertEquals(1, process._2.exitInitialStateCount);
        assertEquals(1, process._2.enterCountStateCount);
    }

    @Test
    public void given_in_INITIAL_state_when_a_TAIL_event_should_remain_in_INITAL_state() throws Exception {
        final Tuple<State, RuntimeState> startState = stateMachine.start(new RuntimeState());
        final Tuple<State, RuntimeState> process = stateMachine.process(startState, TAILS);

        assertEquals(INITIAL, process._1);
        assertEquals(0, process._2.heads);
        assertEquals(0, process._2.tails);
        assertEquals(0, process._2.exitInitialStateCount);
        assertEquals(0, process._2.enterCountStateCount);
    }

    @Test
    public void when_in_INITIAL_state_given_two_MIRACLE_events_should_move_into_FINAL_state() throws Exception {
        final Tuple<State, RuntimeState> startState = stateMachine.start(new RuntimeState());
        final Tuple<State, RuntimeState> process = stateMachine.process(startState, MIRACLE, MIRACLE);

        assertEquals(FINAL, process._1);
        assertEquals(0, process._2.heads);
        assertEquals(0, process._2.tails);
        assertEquals(1, process._2.exitInitialStateCount);
        assertEquals(1, process._2.enterCountStateCount);
    }
}

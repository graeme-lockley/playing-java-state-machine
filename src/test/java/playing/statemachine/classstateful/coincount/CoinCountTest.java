package playing.statemachine.classstateful.coincount;

import org.junit.Before;
import org.junit.Test;
import playing.statemachine.classstateful.StateMachine;
import playing.util.Tuple;

import static org.junit.Assert.assertEquals;
import static playing.statemachine.classstateful.coincount.State.*;

public class CoinCountTest {
    private StateMachine<State, RuntimeState> stateMachine;

    @Before
    public void before() {
        stateMachine = new StateMachine.Builder<State, RuntimeState>()
                .initialState(INITIAL)
                .onState(INITIAL, s -> s
                        .onEvent(MiracleToss.class).changeTo(COUNT).action(RuntimeState::toss)
                        .onExitAction(RuntimeState::exitInitialState))
                .onState(COUNT, s -> s
                        .onEntryAction(RuntimeState::enterCountState)
                        .onEvent(HeadsToss.class).action(RuntimeState::incHeads)
                        .onEvent(TailsToss.class).action(RuntimeState::incTails)
                        .onEvent(MiracleToss.class).changeTo(FINAL).action(RuntimeState::toss))
                .build();
    }

    @Test
    public void when_in_INITIAL_state_given_a_MIRACLE_event_should_move_into_COUNT_state() throws Exception {
        final Tuple<State, RuntimeState> startState = stateMachine.start(new RuntimeState());
        final Tuple<State, RuntimeState> process = stateMachine.process(startState, new MiracleToss(1));

        assertEquals(COUNT, process._1);
        assertEquals(0, process._2.heads);
        assertEquals(0, process._2.tails);
        assertEquals(1, process._2.exitInitialStateCount);
        assertEquals(1, process._2.enterCountStateCount);
        assertEquals(1, process._2.totalDuration);
    }

    @Test
    public void given_in_INITIAL_state_when_a_TAIL_event_should_remain_in_INITAL_state() throws Exception {
        final Tuple<State, RuntimeState> startState = stateMachine.start(new RuntimeState());
        final Tuple<State, RuntimeState> process = stateMachine.process(startState, new TailsToss(2));

        assertEquals(INITIAL, process._1);
        assertEquals(0, process._2.heads);
        assertEquals(0, process._2.tails);
        assertEquals(0, process._2.exitInitialStateCount);
        assertEquals(0, process._2.enterCountStateCount);
        assertEquals(0, process._2.totalDuration);
    }

    @Test
    public void when_in_INITIAL_state_given_two_MIRACLE_events_should_move_into_FINAL_state() throws Exception {
        final Tuple<State, RuntimeState> startState = stateMachine.start(new RuntimeState());
        final Tuple<State, RuntimeState> process = stateMachine.process(startState, new MiracleToss(3), new MiracleToss(4));

        assertEquals(FINAL, process._1);
        assertEquals(0, process._2.heads);
        assertEquals(0, process._2.tails);
        assertEquals(1, process._2.exitInitialStateCount);
        assertEquals(1, process._2.enterCountStateCount);
        assertEquals(7, process._2.totalDuration);
    }

    @Test
    public void when_in_INITIAL_state_given_events_MIRACLE_HEAD_HEAD_TAILS_MIRACLE_should_move_into_FINAL_state_with_correct_counts() throws Exception {
        final Tuple<State, RuntimeState> startState = stateMachine.start(new RuntimeState());
        final Tuple<State, RuntimeState> process = stateMachine.process(startState, new MiracleToss(5), new HeadsToss(6), new HeadsToss(7), new TailsToss(8), new MiracleToss(9));

        assertEquals(FINAL, process._1);
        assertEquals(2, process._2.heads);
        assertEquals(1, process._2.tails);
        assertEquals(1, process._2.exitInitialStateCount);
        assertEquals(4, process._2.enterCountStateCount);
        assertEquals(35, process._2.totalDuration);
    }
}

package playing.statemachine.classstateful.cointosscondition;

class RuntimeState {
    final int heads;
    final int tails;
    final int exitInitialStateCount;
    final int enterCountStateCount;
    final long totalDuration;

    RuntimeState() {
        this(0, 0, 0, 0, 0);
    }

    private RuntimeState(int heads, int tails, int exitInitialStateCount, int enterCountStateCount, long totalDuration) {
        this.heads = heads;
        this.tails = tails;
        this.exitInitialStateCount = exitInitialStateCount;
        this.enterCountStateCount = enterCountStateCount;
        this.totalDuration = totalDuration;
    }

    RuntimeState toss(CoinToss toss) {
        if (toss.isHeads()) {
            return new RuntimeState(heads + 1, tails, exitInitialStateCount, enterCountStateCount, totalDuration + toss.spinDuration());
        } else if (toss.isTails()) {
            return new RuntimeState(heads, tails + 1, exitInitialStateCount, enterCountStateCount, totalDuration + toss.spinDuration());
        } else {
            return new RuntimeState(heads, tails, exitInitialStateCount, enterCountStateCount, totalDuration + toss.spinDuration());
        }
    }

    RuntimeState exitInitialState() {
        return new RuntimeState(heads, tails, exitInitialStateCount + 1, enterCountStateCount, totalDuration);
    }

    RuntimeState enterCountState() {
        return new RuntimeState(heads, tails, exitInitialStateCount, enterCountStateCount + 1, totalDuration);
    }
}


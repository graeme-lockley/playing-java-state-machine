package playing.statemachine.coincount;

class RuntimeState {
    final int heads;
    final int tails;
    final int exitInitialStateCount;
    final int enterCountStateCount;

    RuntimeState() {
        this(0, 0, 0, 0);
    }

    private RuntimeState(int heads, int tails, int exitInitialStateCount, int enterCountStateCount) {
        this.heads = heads;
        this.tails = tails;
        this.exitInitialStateCount = exitInitialStateCount;
        this.enterCountStateCount = enterCountStateCount;
    }

    RuntimeState incHeads() {
        return new RuntimeState(heads + 1, tails, exitInitialStateCount, enterCountStateCount);
    }

    RuntimeState incTails() {
        return new RuntimeState(heads, tails + 1, exitInitialStateCount, enterCountStateCount);
    }

    RuntimeState exitInitialState() {
        return new RuntimeState(heads, tails, exitInitialStateCount + 1, enterCountStateCount);
    }

    RuntimeState enterCountState() {
        return new RuntimeState(heads, tails, exitInitialStateCount, enterCountStateCount + 1);
    }
}


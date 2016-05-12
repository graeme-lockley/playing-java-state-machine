package playing.statemachine.classstateful.coincount;

class CoinToss {
    private final long spinDuration;

    CoinToss(long spinDuration) {
        this.spinDuration = spinDuration;
    }

    long spinDuration() {
        return this.spinDuration;
    }
}

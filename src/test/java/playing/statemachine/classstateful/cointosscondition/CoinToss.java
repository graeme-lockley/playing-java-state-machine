package playing.statemachine.classstateful.cointosscondition;

abstract class CoinToss {
    private final long spinDuration;

    CoinToss(long spinDuration) {
        this.spinDuration = spinDuration;
    }

    long spinDuration() {
        return this.spinDuration;
    }

    abstract boolean isHeads();

    abstract boolean isTails();
}

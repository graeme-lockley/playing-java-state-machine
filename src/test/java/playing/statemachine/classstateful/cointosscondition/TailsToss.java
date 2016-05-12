package playing.statemachine.classstateful.cointosscondition;

class TailsToss extends CoinToss {
    TailsToss(long spinDuration) {
        super(spinDuration);
    }

    @Override
    boolean isHeads() {
        return false;
    }

    @Override
    boolean isTails() {
        return true;
    }
}

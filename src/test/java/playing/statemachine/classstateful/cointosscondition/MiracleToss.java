package playing.statemachine.classstateful.cointosscondition;

class MiracleToss extends CoinToss {
    MiracleToss(long spinDuration) {
        super(spinDuration);
    }

    @Override
    boolean isHeads() {
        return false;
    }

    @Override
    boolean isTails() {
        return false;
    }
}

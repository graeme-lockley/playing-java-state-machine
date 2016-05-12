package playing.statemachine.classstateful.cointosscondition;

class HeadsToss extends CoinToss {
    HeadsToss(long spinDuration) {
        super(spinDuration);
    }

    @Override
    boolean isHeads() {
        return true;
    }

    @Override
    boolean isTails() {
        return false;
    }
}

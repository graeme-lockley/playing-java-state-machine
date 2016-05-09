package playing.statemachine;

import java.util.Optional;

public interface RuntimeState<EVENT> {
    Optional<RuntimeState<EVENT>> process(EVENT event);
}

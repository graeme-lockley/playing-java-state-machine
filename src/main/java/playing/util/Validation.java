package playing.util;

import java.util.Optional;

public class Validation {
    public static <S> OptionalValidation<S> validate(Optional<S> initial) {
        return new OptionalValidation<>(initial);
    }

    public static class OptionalValidation<S> {
        private final Optional<S> value;

        public OptionalValidation(Optional<S> value) {
            this.value = value;
            if (value == null) {
                throw new IllegalArgumentException("The optional value is null");
            }
        }

        public OptionalValidation isPresent() {
            if (!value.isPresent()) {
                throw new IllegalArgumentException("The optional value is not present");
            }
            return this;
        }
    }
}

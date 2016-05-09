package playing.util;

import java.util.Map;
import java.util.function.Supplier;

public class MapUtil {
    public static <S, T> T getOrElse(Map<S, T> map, S key, Supplier<T> elseValue) {
        T result = map.get(key);
        if (result == null) {
            result = elseValue.get();
            map.put(key, result);
        }
        return result;
    }
}

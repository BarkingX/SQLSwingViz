package util;

import java.util.Map;
import java.util.function.Consumer;

public interface FilterWrapper<V> extends Map.Entry<FilterType, V> {
    @Override
    default V setValue(V value) {
        throw new UnsupportedOperationException();
    }
}

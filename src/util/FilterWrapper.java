package util;

import java.util.Map;

public interface FilterWrapper<V> extends Map.Entry<FilterType, V> {
    @Override
    default V setValue(V value) {
        return null;
    }
}

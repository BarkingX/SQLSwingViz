package util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class UnassignedFilterMap<V> extends LinkedHashMap<FilterType, LinkedHashSet<V>> {
    public UnassignedFilterMap() {}

    public UnassignedFilterMap(FilterType key, LinkedHashSet<V> values) {
        put(key, values);
    }

    public void put(FilterType key, Collection<V> values) {
        put(key, new LinkedHashSet<>(values));
    }
}

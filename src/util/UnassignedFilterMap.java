package util;

import java.util.*;

public class UnassignedFilterMap<V> extends LinkedHashMap<FilterType, Set<V>> {
    public UnassignedFilterMap() {}

    public UnassignedFilterMap(FilterType key, Set<V> values) {
        put(key, new LinkedHashSet<>(values));
    }
}

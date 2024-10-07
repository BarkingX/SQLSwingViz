package util;

import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor
public class UnassignedFilterMap<V> extends LinkedHashMap<FilterType, Set<V>> {
    public UnassignedFilterMap(FilterType key, Set<V> values) {
        put(key, new LinkedHashSet<>(values));
    }

    public UnassignedFilterMap(Map<FilterType, Set<V>> other) {
        super(other);
    }
}

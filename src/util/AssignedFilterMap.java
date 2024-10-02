package util;

import java.util.Collection;
import java.util.LinkedHashMap;

public class AssignedFilterMap<V> extends LinkedHashMap<FilterType, V> {
    public AssignedFilterMap(Collection<? extends FilterWrapper<V>> filterSuppliers) {
        filterSuppliers.forEach(this::put);
    }

    public void put(FilterWrapper<V> filterWrapper) {
        var key = filterWrapper.getKey();
        var value = filterWrapper.getValue();
        super.put(key, value);
    }
}

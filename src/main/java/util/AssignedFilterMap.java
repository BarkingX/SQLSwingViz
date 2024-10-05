package util;

import lombok.NonNull;

import java.util.Collection;
import java.util.LinkedHashMap;

public class AssignedFilterMap<V> extends LinkedHashMap<FilterType, V> {
    public AssignedFilterMap(@NonNull Collection<? extends FilterWrapper<V>> filterSuppliers) {
        filterSuppliers.forEach(this::put);
    }

    public void put(@NonNull FilterWrapper<V> filterWrapper) {
        put(filterWrapper.getKey(), filterWrapper.getValue());
    }
}

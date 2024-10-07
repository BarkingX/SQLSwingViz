package util;

import lombok.NonNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AssignedFilterMap<V> extends LinkedHashMap<FilterType, V> {
    public AssignedFilterMap(@NonNull Collection<? extends FilterWrapper<V>> filterSuppliers) {
        filterSuppliers.forEach(this::put);
    }

    public void put(@NonNull FilterWrapper<V> filterWrapper) {
        put(filterWrapper.getKey(), filterWrapper.getValue());
    }

    public List<V> gets(@NonNull Collection<FilterType> keys) {
        return keys.stream().map(this::get).collect(Collectors.toList());
    }
}

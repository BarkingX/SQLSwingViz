package util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;

public class AssignedFilterMap<V> extends LinkedHashMap<FilterType, V> {
    public AssignedFilterMap(@NotNull Collection<? extends FilterWrapper<V>> filterSuppliers) {
        filterSuppliers.forEach(this::put);
    }

    public void put(@NotNull FilterWrapper<V> filterWrapper) {
        var key = filterWrapper.getKey();
        var value = filterWrapper.getValue();
        super.put(key, value);
    }
}

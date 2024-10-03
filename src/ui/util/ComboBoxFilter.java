package ui.util;

import org.jetbrains.annotations.NotNull;
import util.FilterType;
import util.FilterWrapper;

import javax.swing.*;
import java.util.Set;

public class ComboBoxFilter<V> extends JComboBox<V> implements FilterWrapper<V> {
    private final FilterType key;

    public ComboBoxFilter(FilterType key, @NotNull Set<V> values) {
        this.key = key;
        setModel(new DefaultComboBoxModel<>((V[]) values.toArray()));
    }

    @Override
    public FilterType getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return (V) getSelectedItem();
    }

}

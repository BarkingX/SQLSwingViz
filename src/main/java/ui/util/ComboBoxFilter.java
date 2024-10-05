package ui.util;

import lombok.Getter;
import lombok.NonNull;
import util.FilterType;
import util.FilterWrapper;

import javax.swing.*;
import java.util.Set;


@Getter
public class ComboBoxFilter<V> extends JComboBox<V> implements FilterWrapper<V> {
    private final FilterType key;

    @SuppressWarnings("unchecked")
    public ComboBoxFilter(FilterType key, @NonNull Set<V> values) {
        this.key = key;
        setModel(new DefaultComboBoxModel<>((V[]) values.toArray()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getValue() {
        return (V) getSelectedItem();
    }
}

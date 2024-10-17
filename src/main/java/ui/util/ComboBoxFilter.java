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

    public ComboBoxFilter(@NonNull FilterType key, @NonNull V[] values) {
        this.key = key;
        setModel(new DefaultComboBoxModel<>(values));
    }

    @SuppressWarnings("unchecked")
    public ComboBoxFilter(@NonNull FilterType key, @NonNull Set<V> values) {
        this(key, (V[]) values.toArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getValue() {
        return (V) getSelectedItem();
    }
}

package ui.util;

import util.FilterType;
import util.FilterWrapper;

import javax.swing.*;
import java.util.Set;

public class ComboBoxFilter extends JComboBox<String> implements FilterWrapper<String> {
    private FilterType key;

    public ComboBoxFilter(FilterType key, Set<String> values) {
        put(key, values);
    }

    public void put(FilterType key, Set<String> values) {
        this.key = key;
        var model = new DefaultComboBoxModel<String>();
        model.addAll(values);
        setModel(model);
    }

    @Override
    public FilterType getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return (String) getSelectedItem();
    }
}

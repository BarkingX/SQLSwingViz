package ui.sub;

import org.jetbrains.annotations.NotNull;
import util.FilterWrapper;
import ui.util.ComboBoxFilter;
import ui.util.Utils;
import util.AssignedFilterMap;
import util.UnassignedFilterMap;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.print.PrinterException;
import java.util.LinkedList;
import java.util.function.Consumer;

public class DataDisplayPanel extends JPanel {
    private static final byte MAX_SIZEABLE_COL = 10;
    private final LinkedList<ComboBoxFilter<String>> comboBoxFilters = new LinkedList<>();
    private final JPanel filterPanel = new JPanel(new GridLayout());
    private final JTable dataTable = new JTable();
    private final JLabel count = new JLabel("0 条记录");
    private ComboBoxFilter<String> mainFilter;

    public DataDisplayPanel() {
        setLayout(new BorderLayout());

        var tablePane = new JScrollPane(dataTable);
        tablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(tablePane, BorderLayout.CENTER);
        add(Utils.makeJPanel(count), BorderLayout.SOUTH);
        add(filterPanel, BorderLayout.NORTH);
    }

    public void configureMainFilter(@NotNull UnassignedFilterMap<String> mainFilterMap,
                                    @NotNull Consumer<FilterWrapper<String>> afterSelection) {
        var rawMainFilter = mainFilterMap.entrySet().iterator().next();

        mainFilter = new ComboBoxFilter<>(rawMainFilter.getKey(), rawMainFilter.getValue());
        mainFilter.addActionListener(e -> {
            comboBoxFilters.forEach(filterPanel::remove);
            comboBoxFilters.clear();
            afterSelection.accept(mainFilter);
        });
        filterPanel.add(mainFilter);
    }

    public void configureFilters(@NotNull UnassignedFilterMap<String> unassignedFilterMap) {
        unassignedFilterMap.forEach((t, v) -> comboBoxFilters.add(new ComboBoxFilter<>(t, v)));
        comboBoxFilters.forEach(filterPanel::add);
    }

    public void configureQueryAction(@NotNull final Consumer<FilterWrapper<String>> fireQuery) {
        filterPanel.add(Utils.makeJButton("查询", () -> fireQuery.accept(mainFilter)));
    }

    public void display(@NotNull TableModel tableModel) {
        dataTable.setModel(tableModel);
        dataTable.setRowSorter(new TableRowSorter<>(tableModel));
        dataTable.setAutoResizeMode(tableModel.getColumnCount() > MAX_SIZEABLE_COL ?
                JTable.AUTO_RESIZE_OFF : JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        dataTable.doLayout();
        count.setText(tableModel.getRowCount() + " 条记录");
    }

    public void print() {
        try {
            dataTable.print(JTable.PrintMode.FIT_WIDTH);
        }
        catch (PrinterException e) {
            e.printStackTrace();
        }
    }

    public @NotNull AssignedFilterMap<String> getSelectedFilter() {
        return new AssignedFilterMap<>(comboBoxFilters);
    }

    public void reset() {
        display(new DefaultTableModel());
        filterPanel.removeAll();
    }
}

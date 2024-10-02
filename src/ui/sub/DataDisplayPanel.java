package ui.sub;

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
    private static final TableModel emptyModel = new DefaultTableModel();
    private final LinkedList<ComboBoxFilter> comboBoxFilters;
    private final JPanel filterPanel;
    private final JTable dataTable;
    private final JLabel count;
    private ComboBoxFilter mainFilter;

    public DataDisplayPanel() {
        setLayout(new BorderLayout());

        comboBoxFilters = new LinkedList<>();
        filterPanel = new JPanel(new GridLayout());
        dataTable = new JTable();
        count = new JLabel("0 条记录");
        var tablePane = new JScrollPane(dataTable);
        var statusPanel = new JPanel();

        tablePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        statusPanel.add(count);
        add(tablePane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        add(filterPanel, BorderLayout.NORTH);
    }

    public void configureMainFilter(UnassignedFilterMap<String> mainFilterMap,
                                    Consumer<FilterWrapper<String>> afterSelection) {
        var rawMainFilter = mainFilterMap.entrySet().iterator().next();

        mainFilter = new ComboBoxFilter(rawMainFilter.getKey(), rawMainFilter.getValue());
        mainFilter.addActionListener(e -> {
            comboBoxFilters.forEach(filterPanel::remove);
            comboBoxFilters.clear();
            afterSelection.accept(mainFilter);
        });

        filterPanel.add(mainFilter);
    }

    public void configureFilters(UnassignedFilterMap<String> unassignedFilterMap) {
        unassignedFilterMap.forEach((filterType, values) -> {
            var comboBoxFilter = new ComboBoxFilter(filterType, values);
            comboBoxFilters.add(comboBoxFilter);
        });
        comboBoxFilters.forEach(filterPanel::add);
    }

    public void configureQueryAction(final Consumer<FilterWrapper<String>> fireQuery) {
        var queryButton = Utils.makeJButton("查询",  e -> fireQuery.accept(mainFilter));
        filterPanel.add(queryButton);
    }

    public void display(TableModel tableModel) {
        var sorter = new TableRowSorter<>(tableModel);
        dataTable.setModel(tableModel);
        dataTable.setRowSorter(sorter);
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

    public boolean hasData() {
        var model = dataTable.getModel();
        return model != null && model.getRowCount() != 0;
    }

    public AssignedFilterMap<String> getSelectedFilter() {
        return new AssignedFilterMap<>(comboBoxFilters);
    }

    public void reset() {
        display(emptyModel);
        filterPanel.removeAll();
    }
}

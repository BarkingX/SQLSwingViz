package ui.sub;

import lombok.NonNull;
import model.QueryModel;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import util.FilterWrapper;
import ui.util.ComboBoxFilter;
import ui.util.UiUtil;
import util.AssignedFilterMap;
import util.UnassignedFilterMap;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.util.*;
import java.util.function.Consumer;

import static com.google.common.collect.ImmutableMap.ofEntries;
import static java.util.Map.entry;
import static ui.util.UiUtil.showErrorDialog;

public class DataDisplayPanel extends JXPanel {
    private final LinkedList<ComboBoxFilter<String>> comboBoxFilters = new LinkedList<>();
    private final JXPanel filterPanel = new JXPanel(new GridLayout());
    private final JXTable dataTable = new JXTable();
    private final JXLabel statusLabel = new JXLabel();
    private ComboBoxFilter<String> mainFilter;

    public DataDisplayPanel() {
        setLayout(new BorderLayout());
        configureDatatable();
        add(new JScrollPane(dataTable), BorderLayout.CENTER);
        add(UiUtil.makeJXPanel(statusLabel), BorderLayout.SOUTH);
        add(filterPanel, BorderLayout.NORTH);
    }

    private void configureDatatable() {
        dataTable.setHighlighters(HighlighterFactory.createSimpleStriping());
        dataTable.setColumnControlVisible(true);
        dataTable.setSortsOnUpdates(true);
        dataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dataTable.setRowSelectionAllowed(true);
        dataTable.setColumnSelectionAllowed(true);
    }

    public void updateStatus(@NonNull String status) {
        statusLabel.setText(status);
    }

    public void configureMainFilter(@NonNull UnassignedFilterMap<String> mainFilterMap,
                                    @NonNull Consumer<FilterWrapper<String>> afterSelection) {
        var rawMainFilter = mainFilterMap.entrySet().iterator().next();
        mainFilter = new ComboBoxFilter<>(rawMainFilter.getKey(), rawMainFilter.getValue());

        mainFilter.addActionListener(e -> {
            comboBoxFilters.forEach(filterPanel::remove);
            comboBoxFilters.clear();
            afterSelection.accept(mainFilter);
        });
        filterPanel.add(mainFilter);
    }

    public void configureFilters(@NonNull UnassignedFilterMap<String> unassignedFilterMap) {
        unassignedFilterMap.forEach((t, v) -> comboBoxFilters.add(new ComboBoxFilter<>(t, v)));
        comboBoxFilters.forEach(filterPanel::add);
    }

    public void configureQueryAction(@NonNull final Consumer<FilterWrapper<String>> fireQuery) {
        filterPanel.add(UiUtil.makeJButton("查询", () -> fireQuery.accept(mainFilter)));
    }

    public void configureDeleteAction(@NonNull final Runnable fireDelete) {
        filterPanel.add(UiUtil.makeJButton("删除", fireDelete));
    }

    public void display(@NonNull TableModel tableModel) {
        dataTable.setModel(tableModel);
        dataTable.doLayout();

        String filterStatus = comboBoxFilters.stream().allMatch(FilterWrapper::isEmpty) ? "无筛选条件" : "已应用筛选";
        updateStatus(String.format("%d 条记录 | %s", tableModel.getRowCount(), filterStatus));
    }

    public void print() {
        try {
            dataTable.print(JTable.PrintMode.FIT_WIDTH);
        }
        catch (PrinterException e) {
            showErrorDialog(this, e.getMessage(), "打印失败");
        }
    }

    public @NonNull Collection<Map<String, Object>> getSelectedRows() {
        int[] selectedRows = dataTable.getSelectedRows();
        if (selectedRows.length == 0) {
            return Collections.emptyList();
        }

        var model = (QueryModel) dataTable.getModel();
        var result = new ArrayList<Map<String, Object>>(selectedRows.length);
        for (int viewRow : selectedRows) {
            var modelRow = dataTable.convertRowIndexToModel(viewRow);
            result.add(ofEntries(entry(model.getColumnName(0), model.getValueAt(modelRow, 0)),
                                entry(model.getColumnName(1), model.getValueAt(modelRow, 1))));
        }
        return result;
    }

    public @NonNull String getTableName() {
        var model = dataTable.getModel();
        return model instanceof QueryModel? ((QueryModel) model).getTableName() : "";
    }

    public @NonNull AssignedFilterMap<String> getSelectedFilter() {
        return new AssignedFilterMap<>(comboBoxFilters);
    }

    public @NonNull String getMessageAfterDeletion() {
        return String.format("删除成功: 已删除 %d 条记录 | 重新查询以确认删除结果",
                dataTable.getSelectedRowCount());
    }

    public void reset() {
        display(new DefaultTableModel());
        filterPanel.removeAll();
        updateStatus("初始状态");
    }
}

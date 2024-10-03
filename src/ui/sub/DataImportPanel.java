package ui.sub;

import org.jetbrains.annotations.NotNull;
import ui.abs.DialogWrapper;
import ui.util.Utils;
import util.Option;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Objects;

public class DataImportPanel extends DialogWrapper {
    private final JComboBox<String> tableNamesBox = new JComboBox<>();
    private final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
    private final JButton submitBtn;
    private int result;

    public DataImportPanel() {
        setLayout(new FlowLayout());
        fileChooser.setFileFilter(new FileNameExtensionFilter("逗号分隔符文件", "csv"));

        submitBtn = Utils.makeJButton("确认提交", e -> processSubmission());
        var chooseFileBtn = Utils.makeJButton("选择本地文件", e -> showOpenFileDialog());
        Utils.addAll(this, tableNamesBox, chooseFileBtn, submitBtn);
    }

    private void showOpenFileDialog() {
        result = fileChooser.showOpenDialog(this);
    }

    private void processSubmission() {
        var option = result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null ? Option.OK : Option.ERROR;
        setOption(option);
        closeDialog();
    }

    public void populateTableNameComboBox(@NotNull Collection<String> tableNames) {
        tableNames.forEach(tableNamesBox::addItem);
    }

    public @NotNull String getSelectedTableName() {
        return (String) Objects.requireNonNull(tableNamesBox.getSelectedItem());
    }

    public @NotNull File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }

    @Override
    public @NotNull Option showDialog(@NotNull Component parent) {
        return showDialog(parent, submitBtn, "数据录入");
    }

    @Override
    protected void reset() {
        result = JFileChooser.CANCEL_OPTION;
        setOption(Option.EXIT);
    }

    public boolean notInitiated() {
        return tableNamesBox.getItemCount() == 0;
    }

    public void resetAfterReEnter() {
        tableNamesBox.removeAllItems();
    }
}

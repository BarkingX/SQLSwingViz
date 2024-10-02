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

public class DataImportPanel extends DialogWrapper {
    private final JComboBox<String> tableNamesBox;
    private final JFileChooser fileChooser;
    private final JButton submit;
    private int result;

    public DataImportPanel() {
        setLayout(new FlowLayout());
        tableNamesBox = new JComboBox<>();
        fileChooser = new JFileChooser("D:\\Data\\database\\io\\port\\data");
        submit = Utils.makeJButton("确认提交", e -> processSubmission());
        var chooseFile = Utils.makeJButton("选择本地文件", e -> showOpenFileDialog());

        fileChooser.setFileFilter(new FileNameExtensionFilter("逗号分隔符文件", "csv"));
        Utils.addAll(this, tableNamesBox, chooseFile, submit);
    }

    private void showOpenFileDialog() {
        result = fileChooser.showOpenDialog(this);
    }

    private void processSubmission() {
        if (result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null)
            setOption(Option.OK);
        else setOption(Option.ERROR);
        closeDialog();
    }

    public void populateTableNameComboBox(@NotNull Collection<String> tableNames) {
        tableNames.forEach(tableNamesBox::addItem);
    }

    public String getSelectedTableName() {
        return (String) tableNamesBox.getSelectedItem();
    }

    public File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }

    @Override
    public @NotNull Option showDialog(@NotNull Component parent) {
        return showDialog(parent, submit, "数据录入");
    }

    @Override
    protected void reset() {
        result = JFileChooser.CANCEL_OPTION;
        setOption(Option.EXIT);
    }

    public void resetAfterReEnter() {
        tableNamesBox.removeAllItems();
    }
}

package ui.sub;

import lombok.NonNull;
import ui.abs.DialogWrapper;
import ui.util.UiUtil;
import ui.util.Option;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Objects;

public class DataImportDialog extends DialogWrapper {
    private final JComboBox<String> tableNamesBox = new JComboBox<>();
    private final JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
    private final JButton submitBtn;
    private int result;

    public DataImportDialog() {
        setLayout(new FlowLayout());
        fileChooser.setFileFilter(new FileNameExtensionFilter("逗号分隔符文件", "csv"));

        submitBtn = UiUtil.makeJButton("确认提交", this::processSubmission);
        var chooseFileBtn = UiUtil.makeJButton("选择本地文件", this::showOpenFileDialog);
        UiUtil.addAll(this, tableNamesBox, chooseFileBtn, submitBtn);
    }

    private void processSubmission() {
        setOption(result == JFileChooser.APPROVE_OPTION && fileChooser.getSelectedFile() != null
                ? Option.OK : Option.ERROR);
    }

    private void showOpenFileDialog() {
        result = fileChooser.showOpenDialog(this);
    }

    @Override
    protected void reset() {
        result = JFileChooser.CANCEL_OPTION;
        setOption(Option.EXIT);
    }

    @Override
    protected @NonNull JButton getDefaultButton() {
        return submitBtn;
    }

    @Override
    protected @NonNull String getTitle() {
        return "本地数据导入";
    }

    public void populateTableNameComboBox(@NonNull Collection<String> tableNames) {
        tableNames.forEach(tableNamesBox::addItem);
    }

    public @NonNull String getSelectedTableName() {
        return (String) Objects.requireNonNull(tableNamesBox.getSelectedItem());
    }

    public @NonNull File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }

    public boolean notInitiated() {
        return tableNamesBox.getItemCount() == 0;
    }
}

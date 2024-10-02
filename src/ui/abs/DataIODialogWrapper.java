package ui.abs;

import org.jetbrains.annotations.NotNull;
import ui.sub.DataIOPanel;
import ui.util.IColumnConstants;
import ui.util.Utils;
import util.MetadataSupplier;
import util.Option;

import javax.swing.*;
import java.awt.*;

public abstract class DataIODialogWrapper extends DialogWrapper implements MetadataSupplier<String>, IColumnConstants {
    private final DataIOPanel dataIOPanel;
    private final JButton okButton;

    public DataIODialogWrapper() {
        dataIOPanel = new DataIOPanel(this);
        okButton = Utils.makeJButton("确认", e -> onOkOperation());
        var cancelButton = Utils.makeJButton("取消", e -> onCancelOperation());
        var buttonPanel = new JPanel();
        Utils.addAll(buttonPanel, okButton, cancelButton);

        add(dataIOPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void onOkOperation() {
        setOption(checkIntegrity() ? Option.OK : Option.ERROR);
        closeDialog();
    }

    public abstract boolean checkIntegrity();

    public void onCancelOperation() {
        setOption(Option.CANCEL);
        closeDialog();
    }

    public void replaceWithPasswordField(int column) {
        dataIOPanel.replaceWithPasswordField(column);
    }

    public void setEditableFor(int column, boolean isEditable) {
        dataIOPanel.setEditableFor(column, isEditable);
    }

    public abstract String getTitle();

    public @NotNull String getTextOf(int column) {
        return dataIOPanel.getTextOf(column);
    }

    public boolean haveText(int from, int to) {
        assert to <= dataIOPanel.getMetadata().size();
        for (int col = from; col <= to; col++) {
            if (getTextOf(col).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public @NotNull Option showDialog(@NotNull Component parent) {
        return showDialog(parent, okButton, getTitle());
    }

    @Override
    protected void reset() {
        setOption(Option.EXIT);
        dataIOPanel.resetAllFields();
    }
}

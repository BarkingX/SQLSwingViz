package ui.abs;

import org.jetbrains.annotations.NotNull;
import ui.sub.DataIOPanel;
import ui.util.IColumnConstants;
import ui.util.Utils;
import util.MetadataSupplier;
import util.Option;

import javax.swing.*;
import java.awt.*;

public abstract class DataIODialogWrapper extends DialogWrapper
        implements MetadataSupplier<String>, IColumnConstants {
    private final DataIOPanel dataIOPanel = new DataIOPanel(this);
    private final JButton okButton;

    public DataIODialogWrapper() {
        okButton = Utils.makeJButton("确认", this::onOkOperation);
        var cancelButton = Utils.makeJButton("取消", this::onCancelOperation);

        dataIOPanel.replaceWithPasswordField(PASSWORD_COLUMN);
        add(dataIOPanel, BorderLayout.CENTER);
        add(Utils.makeJPanel(okButton, cancelButton), BorderLayout.SOUTH);
    }

    public abstract void onOkOperation();

    public void onCancelOperation() {
        setOption(Option.CANCEL);
        closeDialog();
    }

    public abstract String getTitle();

    public @NotNull String getTextOf(int column) {
        return dataIOPanel.getTextOf(column);
    }

    public boolean allHaveText(int from, int to) {
        assert to <= dataIOPanel.getMetadata().size();
        for (int col = from; col <= to; col++) {
            if (getTextOf(col).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public JButton getDefaultButton() {
        return okButton;
    }

    @Override
    public @NotNull Option showDialog(@NotNull Component parent) {
        return showDialog(parent, getTitle());
    }

    @Override
    protected void reset() {
        setOption(Option.EXIT);
        dataIOPanel.resetAllFields();
    }
}

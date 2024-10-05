package ui.abs;

import lombok.NonNull;
import ui.sub.DataIOPanel;
import ui.util.IColumnConstants;
import ui.util.Utils;
import util.MetadataSupplier;
import util.Option;

import javax.swing.*;
import java.awt.*;
import java.util.stream.IntStream;

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

    @Override
    protected void reset() {
        setOption(Option.EXIT);
        dataIOPanel.resetAllFields();
    }

    @Override
    protected @NonNull JButton getDefaultButton() {
        return okButton;
    }

    public boolean allHaveText(int from, int to) {
        assert to <= dataIOPanel.getMetadata().size();
        return IntStream.rangeClosed(from, to).noneMatch(col -> getTextOf(col).isEmpty());
    }

    public @NonNull String getTextOf(int column) {
        return dataIOPanel.getTextOf(column);
    }
}

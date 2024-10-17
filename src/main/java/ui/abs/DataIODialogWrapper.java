package ui.abs;

import lombok.NonNull;
import ui.sub.DataIOPanel;
import ui.util.IoTypeConstants;
import util.MetadataSupplier;
import ui.util.Option;

import javax.swing.*;
import java.awt.*;
import java.util.stream.IntStream;

import static ui.util.UiUtil.makeJButton;
import static ui.util.UiUtil.makeJXPanel;

public abstract class DataIODialogWrapper extends DialogWrapper implements MetadataSupplier<String> {
    private final DataIOPanel dataIOPanel = new DataIOPanel(this);
    private final JButton okButton = makeJButton("确认", this::onOkOperation);

    @Override
    protected void reset() {
        setOption(Option.EXIT);
        dataIOPanel.resetAllFields();
    }

    @Override
    protected @NonNull JButton getDefaultButton() {
        return okButton;
    }

    public DataIODialogWrapper() {
        add(dataIOPanel, BorderLayout.CENTER);
        add(makeJXPanel(okButton, makeJButton("取消", this::onCancelOperation)), BorderLayout.SOUTH);
    }

    public abstract void onOkOperation();

    public void onCancelOperation() {
        setOption(Option.CANCEL);
        closeDialog();
    }

    public void replaceWith(@NonNull IoTypeConstants index, @NonNull Component component) {
        dataIOPanel.replaceWith(index.value, component);
    }

    public boolean allHaveText(@NonNull IoTypeConstants from, @NonNull IoTypeConstants to) {
        assert to.value <= dataIOPanel.getMetadata().size();
        return IntStream.rangeClosed(from.value, to.value)
                        .noneMatch(col -> getTextOf(col).isEmpty());
    }

    public @NonNull String getTextOf(@NonNull IoTypeConstants index) {
        return getTextOf(index.value);
    }

    public @NonNull String getTextOf(int index) {
        return dataIOPanel.getTextOf(index);
    }

    public void setPasswordField(@NonNull IoTypeConstants index) {
        dataIOPanel.replaceWithPasswordField(index.value);
    }
}

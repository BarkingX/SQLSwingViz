package ui.abs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Option;
import ui.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;

public abstract class DialogWrapper extends JPanel {
    private JDialog dialog;
    private Option option;

    public DialogWrapper() {
        setLayout(new BorderLayout());
    }

    public void setOption(@NotNull Option o) {
        option = o;
    }

    public void setDialogIconImage(@Nullable Image icon) {
        dialog.setIconImage(icon);
    }

    public abstract @NotNull Option showDialog(Component parent);

    protected @NotNull Option showDialog(Component parent, JButton defaultButton, String title) {
        reset();
        if (needInitiate(ancestorOf(parent))) {
            initiateDialog(ancestorOf(parent), defaultButton);
        }
        Utils.centerWindow(dialog);
        dialog.setTitle(title);
        dialog.setVisible(true);
        return option;
    }

    protected abstract void reset();

    protected void initiateDialog(Component parent, JButton defaultButton) {
        dialog = new JDialog(ancestorOf(parent), true);
        dialog.add(this);
        dialog.getRootPane().setDefaultButton(defaultButton);
        dialog.pack();
    }
    private @NotNull Frame ancestorOf(@NotNull Component parent) {
        return parent instanceof Frame ? (Frame) parent :
                (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
    }
    private boolean needInitiate(@NotNull Frame owner) {
        return dialog == null || dialog.getOwner() != owner;
    }

    public void closeDialog() {
        dialog.setVisible(false);
    }

    public void addWindowListener(WindowListener l) {
        dialog.addWindowListener(l);
    }
}

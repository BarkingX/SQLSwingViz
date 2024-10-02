package ui.abs;

import org.jetbrains.annotations.NotNull;
import util.Option;
import ui.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;
import java.util.function.Predicate;

public abstract class DialogWrapper extends JPanel {
    private WindowListener windowListener;
    private JDialog dialog;
    private Image icon;
    private Option option;

    public DialogWrapper() {
        setLayout(new BorderLayout());
    }

    public DialogWrapper(@NotNull WindowListener l) {
        this();
        windowListener = l;
    }

    public void setOption(@NotNull Option o) {
        option = o;
    }
    public void setDialogIconImage(@NotNull Image icon) {
        this.icon = icon;
    }

    public abstract @NotNull Option showDialog(Component parent);

    protected @NotNull Option showDialog(Component parent, JButton defaultButton, String title) {
        reset();
        initiateDialogIf(parent, defaultButton, this::needInitiate);
        Utils.centerWindow(dialog);
        dialog.setTitle(title);
        dialog.setVisible(true);
        return option;
    }

    protected abstract void reset();

    private void initiateDialogIf(Component parent, JButton defaultButton,
                                  @NotNull Predicate<Frame> predicate) {
        Frame owner = ancestorOf(parent);
        if (predicate.test(owner)) {
            dialog = new JDialog(owner, true);
            dialog.add(this);
            if (windowListener != null) dialog.addWindowListener(windowListener);
            dialog.getRootPane().setDefaultButton(defaultButton);
            if (icon != null) dialog.setIconImage(icon);
            dialog.pack();
        }
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
}

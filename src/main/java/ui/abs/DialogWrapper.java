package ui.abs;

import lombok.NonNull;
import lombok.Setter;
import util.Option;
import ui.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;

public abstract class DialogWrapper extends JPanel {
    private JDialog dialog;
    @Setter private Option option;

    public DialogWrapper() {
        setLayout(new BorderLayout());
    }

    public void setIconImage(Image icon) {
        dialog.setIconImage(icon);
    }

    public void addWindowListener(WindowListener l) {
        dialog.addWindowListener(l);
    }

    public void closeDialog() {
        dialog.setVisible(false);
    }

    public @NonNull Option showDialog(Component parent) {
        reset();
        var notInitiated = dialog == null || dialog.getOwner() != ancestorOf(parent);
        if (notInitiated) {
            initiateDialog(ancestorOf(parent));
        }
        Utils.centerWindow(dialog);
        dialog.setTitle(getTitle());
        dialog.setVisible(true);
        return option;
    }

    protected abstract void reset();

    private @NonNull Frame ancestorOf(@NonNull Component parent) {
        return parent instanceof Frame ? (Frame) parent :
                (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
    }

    protected void initiateDialog(Component parent) {
        dialog = new JDialog(ancestorOf(parent), true);
        dialog.add(this);
        dialog.getRootPane().setDefaultButton(getDefaultButton());
        dialog.pack();
    }

    protected abstract @NonNull JButton getDefaultButton();

    protected abstract @NonNull String getTitle();
}

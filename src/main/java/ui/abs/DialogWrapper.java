package ui.abs;

import lombok.NonNull;
import lombok.Setter;
import org.jdesktop.swingx.JXPanel;
import ui.util.Option;
import ui.util.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;

import static javax.swing.SwingUtilities.getAncestorOfClass;

public abstract class DialogWrapper extends JXPanel {
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
        UiUtil.centerWindow(dialog);
        dialog.setTitle(getTitle());
        dialog.setVisible(true);
        return option;
    }

    protected abstract void reset();

    private @NonNull Frame ancestorOf(@NonNull Component parent) {
        return parent instanceof Frame ? (Frame) parent : (Frame) getAncestorOfClass(Frame.class, parent);
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

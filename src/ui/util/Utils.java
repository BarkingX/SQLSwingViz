package ui.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.IconType;
import util.Option;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class Utils {
    private Utils() {}

    public static void centerWindow(@NotNull Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - window.getWidth()) / 2;
        int y = (screen.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    public static void addAll(JComponent container, JComponent @NotNull ... components) {
        for (var component : components) container.add(component);
    }

    public static JButton makeJButton(String text, ActionListener l) {
        var button = new JButton(text);
        button.addActionListener(l);
        return button;
    }

    public static JMenuItem makeJMenuItem(String text, ActionListener listener) {
        var menuItem = new JMenuItem(text);
        menuItem.addActionListener(listener);
        return menuItem;
    }

    public static JMenu makeJMenu(String text, JMenuItem... items) {
        var menu = new JMenu(text);
        for (var item : items) menu.add(item);
        return menu;
    }

    public static void showDialog(@NotNull Supplier<Option> showDialog,
                                   Option success, @Nullable Runnable afterSuccess,
                                   Option failed, @Nullable Runnable afterFailed) {
        if (showDialog.get() == success && afterSuccess != null) {
            afterSuccess.run();
        }
        else if (showDialog.get() == failed && afterFailed != null) {
            afterFailed.run();
        }
    }

    public static Option showConfirmDialog(Component parent, Object message,
                                           String title, int optionType, int messageType) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
        return result == JOptionPane.YES_OPTION ? Option.OK : Option.CANCEL;
    }

    public static void showErrorDialog(Component parent, Object message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static @NotNull Map<IconType, Image> getIcons() {
        try {
            var pathName = "D:\\Data\\workspace\\java\\projects\\port_application\\src\\resource\\image\\";
            var general = ImageIO.read(new File(pathName + "administrator.jpg"));
            var user = ImageIO.read(new File(pathName + "user.jpg"));
            var official = ImageIO.read(new File(pathName + "official.jpg"));
            var administrator = ImageIO.read(new File(pathName + "administrator.jpg"));

            var logos = new EnumMap<IconType, Image>(IconType.class);
            logos.put(IconType.GENERAL, general);
            logos.put(IconType.USER, user);
            logos.put(IconType.OFFICIAL, official);
            logos.put(IconType.ADMINISTRATOR, administrator);
            return logos;
        }
        catch (IOException e) {
            return Collections.emptyMap();
        }
    }
}

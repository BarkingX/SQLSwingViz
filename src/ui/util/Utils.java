package ui.util;

import util.IconType;
import util.Option;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

public class Utils {
    private Utils() {}

    public static void centerWindow(Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - window.getWidth()) / 2;
        int y = (screen.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    public static void addAll(JComponent container, JComponent... components) {
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

    public static Option showConfirmDialog(Component parentComponent, Object message,
                                           String title, int optionType, int messageType) {
        int result = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType, messageType);
        return result == JOptionPane.YES_OPTION ? Option.OK : Option.CANCEL;
    }

    public static void showErrorDialog(Component parentComponent, Object message, String title) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static EnumMap<IconType, Image> getIcons() {
        var logos = new EnumMap<IconType, Image>(IconType.class);

        try {
            String pathName = "D:\\Data\\java\\projects\\port_application\\src\\resource\\image\\";
            var general = ImageIO.read(new File(pathName + "administrator.jpg"));
            var user = ImageIO.read(new File(pathName + "user.jpg"));
            var official = ImageIO.read(new File(pathName + "official.jpg"));
            var administrator = ImageIO.read(new File(pathName + "administrator.jpg"));

            logos.put(IconType.GENERAL, general);
            logos.put(IconType.USER, user);
            logos.put(IconType.OFFICIAL, official);
            logos.put(IconType.ADMINISTRATOR, administrator);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return logos;
    }
}

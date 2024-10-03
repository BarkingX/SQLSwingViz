package ui.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.IconType;
import util.Option;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utils {
    public static final Map<IconType, Image> ICONS = getIcons();
    private Utils() {}

    public static void centerWindow(@NotNull Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - window.getWidth()) / 2;
        int y = (screen.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    public static JComponent addAll(@NotNull JComponent container,
                                    @NotNull JComponent... components) {
        Arrays.stream(components).forEach(container::add);
        return container;
    }

    private static @NotNull JComponent make(Class<? extends JComponent> clazz, String text,
                                           @NotNull Consumer<JComponent> after) {
        try {
            JComponent component;
            if (AbstractButton.class.isAssignableFrom(clazz)) {
                component = clazz.getDeclaredConstructor(String.class).newInstance(text);
            }
            else {
                component = clazz.getDeclaredConstructor().newInstance();
            }
            after.accept(component);
            return component;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Failed to create an instance of: " + clazz.getName(), e);
        }
    }

    public static @NotNull JPanel makeJPanel(JComponent @NotNull ... items) {
        return (JPanel) make(JPanel.class, null, c -> addAll(c, items));
    }

    public static @NotNull JMenu makeJMenu(String text, JMenuItem @NotNull ... items) {
        return (JMenu) make(JMenu.class, text, c -> addAll(c, items));
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractButton> @NotNull T makeButton(Class<T> clazz, String text,
                                                                   @NotNull Runnable action) {
        return (T) make(clazz, text, c -> ((AbstractButton) c).addActionListener(e -> action.run()));
    }

    public static @NotNull JMenuItem makeJMenuItem(String text, @NotNull Runnable action) {
        return makeButton(JMenuItem.class, text, action);
    }

    public static @NotNull JButton makeJButton(String text, @NotNull Runnable action) {
        return makeButton(JButton.class, text, action);
    }

    public static void showDialog(@NotNull Supplier<Option> optionSupplier,
                                  Option success, @Nullable Runnable afterSuccess,
                                  Option failed, @Nullable Runnable afterFailed) {
        var option = optionSupplier.get();
        if (option == success && afterSuccess != null) {
            afterSuccess.run();
        }
        else if (option == failed && afterFailed != null) {
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

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull WindowListener exitOnClosing(@Nullable Runnable action) {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (action != null) {
                    action.run();
                }
                System.exit(0);
            }
        };
    }

    public static @NotNull Image getIcon(@NotNull IconType type) {
        return ICONS.get(type);
    }
}

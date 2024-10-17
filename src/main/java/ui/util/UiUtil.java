package ui.util;

import lombok.Getter;
import lombok.NonNull;
import model.Role;
import org.jdesktop.swingx.JXPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableMap.ofEntries;
import static com.google.common.collect.Maps.immutableEnumMap;
import static java.util.Map.entry;
import static model.Role.*;

public class UiUtil {
    @Getter(lazy = true) private static final Map<Role, ? extends Image> icons = loadIcons();
    private UiUtil() {}

    public static void centerWindow(@NonNull Window window) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screen.width - window.getWidth()) / 2;
        int y = (screen.height - window.getHeight()) / 2;
        window.setLocation(x, y);
    }

    public static JComponent addAll(@NonNull JComponent container,
                                    @NonNull JComponent... components) {
        Arrays.stream(components).forEach(container::add);
        return container;
    }

    private static @NonNull JComponent make(Class<? extends JComponent> clazz, String text,
                                           @NonNull Consumer<JComponent> after) {
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

    public static @NonNull JXPanel makeJXPanel(JComponent @NonNull ... items) {
        return (JXPanel) make(JXPanel.class, "", c -> addAll(c, items));
    }

    public static @NonNull JMenu makeJMenu(String text, JMenuItem @NonNull ... items) {
        return (JMenu) make(JMenu.class, text, c -> addAll(c, items));
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractButton> @NonNull T makeButton(Class<T> clazz, String text,
                                                                   @NonNull Runnable action) {
        return (T) make(clazz, text, c -> ((AbstractButton) c).addActionListener(e -> action.run()));
    }

    public static @NonNull JMenuItem makeJMenuItem(String text, @NonNull Runnable action) {
        return makeButton(JMenuItem.class, text, action);
    }

    public static @NonNull JButton makeJButton(String text, @NonNull Runnable action) {
        return makeButton(JButton.class, text, action);
    }

    public static void showDialog(@NonNull Supplier<Option> optionSupplier,
                                  Option success, Runnable afterSuccess,
                                  Option failed, Runnable afterFailed) {
        var option = optionSupplier.get();
        if (option == success && afterSuccess != null) {
            afterSuccess.run();
        }
        else if (option == failed && afterFailed != null) {
            afterFailed.run();
        }
    }

    public static @NonNull Option showConfirmDialog(Component parent, Object message,
                                                    String title, int optionType, int messageType) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType);
        return result == JOptionPane.YES_OPTION ? Option.OK : Option.CANCEL;
    }

    public static void showErrorDialog(Component parent, Object message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static @NonNull Map<Role, ? extends Image> loadIcons() {
        try {
            return immutableEnumMap(ofEntries(entry(NULL, readImage("general.jpg")),
                                              entry(USER, readImage("user.jpg")),
                                              entry(OFFICIAL, readImage("official.jpg")),
                                              entry(ADMIN, readImage("administrator.jpg"))));
        }
        catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    static @NonNull Image readImage(@NonNull String name) throws IOException {
        var path = "src/main/resources/image/%1$s";
        return Optional.ofNullable(ImageIO.read(Paths.get(String.format(path, name)).toFile())).orElseThrow();
    }

    public static @NonNull WindowListener exitOnClosing(Runnable action) {
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

    public static @NonNull Image getIcon(@NonNull Role role) {
        return getIcons().get(role);
    }
}

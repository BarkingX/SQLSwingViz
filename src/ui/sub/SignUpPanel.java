package ui.sub;

import org.jetbrains.annotations.NotNull;
import ui.abs.SignPanel;


import java.awt.*;

public class SignUpPanel extends SignPanel {
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 400;

    @Override
    public @NotNull String getTitle() {
        return "注册";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}

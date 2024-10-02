package ui.sub;

import ui.abs.SignPanel;

import java.awt.*;

public class SignInPanel extends SignPanel {
    private static final int DEFAULT_WIDTH = 240;
    private static final int DEFAULT_HEIGHT = 180;

    @Override
    public String getTitle() {
        return "登录";
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}

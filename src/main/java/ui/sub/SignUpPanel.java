package ui.sub;

import lombok.NonNull;
import model.Role;
import model.User;
import ui.abs.SignPanel;

import java.awt.*;

public class SignUpPanel extends SignPanel {
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 400;

    @Override
    public @NonNull String getTitle() {
        return "注册";
    }

    @Override
    public @NonNull String getErrorMessage() {
        return super.getErrorMessage() + "\n- 账号已存在";
    }

    @Override
    public @NonNull User getUser() {
        return super.getUser().withRole(Role.USER);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}

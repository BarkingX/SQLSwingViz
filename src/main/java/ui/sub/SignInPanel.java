package ui.sub;

import lombok.NonNull;
import model.Role;
import model.User;
import ui.abs.SignPanel;

import java.awt.*;

public class SignInPanel extends SignPanel {
    private static final int DEFAULT_WIDTH = 240;
    private static final int DEFAULT_HEIGHT = 180;

    @Override
    @NonNull
    public String getTitle() {
        return "登录";
    }

    @Override
    @NonNull
    public String getErrorMessage() {
        return super.getErrorMessage() + "\n- 账号或密码错误";
    }

    @Override
    public @NonNull User getUser() {
        return super.getUser().withRole(Role.NULL);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}

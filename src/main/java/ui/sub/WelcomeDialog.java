package ui.sub;

import lombok.NonNull;
import lombok.Setter;
import model.Role;
import ui.abs.DialogWrapper;
import ui.util.UiUtil;
import ui.util.Option;
import model.User;

import javax.swing.*;
import java.awt.*;


public class WelcomeDialog extends DialogWrapper {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;
    private final SignInPanel signInPanel = new SignInPanel();
    private final SignUpPanel signUpPanel = new SignUpPanel();
    private final JButton signInButton;
    @Setter private User user;

    public WelcomeDialog() {
        var signUpButton = UiUtil.makeJButton("注册", () -> showSignDialog(Option.SIGNUP));
        signInButton = UiUtil.makeJButton("登录", () -> showSignDialog(Option.SIGNIN));

        var buttonPanel = UiUtil.makeJXPanel(signInButton, signUpButton);
        var greetingPanel = UiUtil.makeJXPanel(new JLabel("Welcome"));
        buttonPanel.setLayout(new GridLayout(2, 1));
        greetingPanel.setBackground(Color.CYAN);
        add(greetingPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void showSignDialog(@NonNull Option option) {
        closeDialog();
        var signPanel = option == Option.SIGNIN ? signInPanel : signUpPanel;
        if (signPanel.showDialog(WelcomeDialog.this) == Option.OK) {
            setOption(option);
        }
        else {
            showDialog(WelcomeDialog.this);
        }
    }

    public @NonNull User getSignInUser() {
        return user == null ? signInPanel.getUser() : user;
    }

    public @NonNull User getSingUpUser() {
        return signUpPanel.getUser();
    }

    public void updateUser(User user) {
        this.user = user;
    }

    public @NonNull String getSignInErrorMessage() {
        return signInPanel.getErrorMessage();
    }

    public @NonNull String getSignUpErrorMessage() {
        return signUpPanel.getErrorMessage();
    }

    @Override
    public @NonNull Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    protected void reset() {
        updateUser(null);
        setOption(Option.EXIT);
    }

    @Override
    protected void initiateDialog(Component parent) {
        super.initiateDialog(parent);
        setIconImage(UiUtil.getIcon(Role.NULL));
        addWindowListener(UiUtil.exitOnClosing(null));
    }

    @Override
    protected @NonNull JButton getDefaultButton() {
        return signInButton;
    }

    @Override
    @NonNull
    public String getTitle() {
        return "欢迎使用本系统";
    }
}
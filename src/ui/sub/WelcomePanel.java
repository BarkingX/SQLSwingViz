package ui.sub;

import org.jetbrains.annotations.NotNull;
import ui.abs.DialogWrapper;
import ui.util.IconSupplier;
import ui.util.Utils;
import util.IconType;
import util.Option;
import util.Profile;
import util.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowListener;


public class WelcomePanel extends DialogWrapper implements IconSupplier {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;
    private final SignInPanel signInPanel;
    private final SignUpPanel signUpPanel;
    private final JButton signInButton;

    public WelcomePanel(WindowListener l) {
        super(l);
        setDialogIconImage(icons.get(IconType.GENERAL));

        signInPanel = new SignInPanel();
        signUpPanel = new SignUpPanel();
        var greetingPanel = new JPanel();
        var buttonPanel = new JPanel(new GridLayout(2, 1));
        var signUpButton = Utils.makeJButton("注册", e -> showSignDialog(Option.SIGNUP));
        signInButton = Utils.makeJButton("登录", e -> showSignDialog(Option.SIGNIN));

        greetingPanel.setBackground(Color.CYAN);
        greetingPanel.add(new JLabel("Welcome"));
        Utils.addAll(buttonPanel, signInButton, signUpButton);
        add(greetingPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public @NotNull Option showDialog(@NotNull Component parent) {
        return showDialog(parent, signInButton, "欢迎使用本系统");
    }

    @Override
    protected void reset() {
        setOption(Option.EXIT);
    }

    public @NotNull User getSignInUser() {
        return signInPanel.getUser();
    }

    public @NotNull User getSingUpUser() {
        return signUpPanel.getUser();
    }

    public @NotNull Profile getRegisteredProfile() {
        return signUpPanel.getProfile();
    }

    private void showSignDialog(Option approveOption) {
        closeDialog();
        var signPanel = approveOption == Option.SIGNIN ? signInPanel : signUpPanel;
        var result = signPanel.showDialog(WelcomePanel.this);
        if (result == Option.OK) setOption(approveOption);
        else showDialog(WelcomePanel.this);
    }

    @Override
    public @NotNull Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}

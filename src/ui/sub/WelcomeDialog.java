package ui.sub;

import org.jetbrains.annotations.NotNull;
import ui.abs.DialogWrapper;
import ui.util.Utils;
import util.IconType;
import util.Option;
import util.User;

import javax.swing.*;
import java.awt.*;


public class WelcomeDialog extends DialogWrapper {
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 200;
    private final SignInPanel signInPanel = new SignInPanel();
    private final SignUpPanel signUpPanel = new SignUpPanel();
    private final JButton signInButton;

    public WelcomeDialog() {
        var signUpButton = Utils.makeJButton("注册", () -> showSignDialog(Option.SIGNUP));
        signInButton = Utils.makeJButton("登录", () -> showSignDialog(Option.SIGNIN));

        var buttonPanel = Utils.makeJPanel(signInButton, signUpButton);
        var greetingPanel = Utils.makeJPanel(new JLabel("Welcome"));
        buttonPanel.setLayout(new GridLayout(2, 1));
        greetingPanel.setBackground(Color.CYAN);
        add(greetingPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public JButton getDefaultButton() {
        return signInButton;
    }

    @Override
    public @NotNull Option showDialog(@NotNull Component parent) {
        return showDialog(parent, "欢迎使用本系统");
    }

    @Override
    protected void initiateDialog(Component parent) {
        super.initiateDialog(parent);
        setDialogIconImage(Utils.getIcon(IconType.GENERAL));
        addWindowListener(Utils.exitOnClosing(null));
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

    private void showSignDialog(@NotNull Option approveOption) {
        closeDialog();
        var signPanel = approveOption == Option.SIGNIN ? signInPanel : signUpPanel;
        var result = signPanel.showDialog(WelcomeDialog.this);
        if (result == Option.OK) {
            setOption(approveOption);
        }
        else {
            showDialog(WelcomeDialog.this);
        }
    }

    @Override
    public @NotNull Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
}

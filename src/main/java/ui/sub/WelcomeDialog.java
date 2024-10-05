package ui.sub;

import lombok.NonNull;
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
        var signUpButton = Utils.makeJButton("注册", this::showSignUpDialog);
        signInButton = Utils.makeJButton("登录", this::showSignInDialog);

        var buttonPanel = Utils.makeJPanel(signInButton, signUpButton);
        var greetingPanel = Utils.makeJPanel(new JLabel("Welcome"));
        buttonPanel.setLayout(new GridLayout(2, 1));
        greetingPanel.setBackground(Color.CYAN);
        add(greetingPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showSignInDialog() {
        showSignDialog(Option.SIGNIN);
    }

    private void showSignUpDialog() {
        showSignDialog(Option.SIGNUP);
    }

    private void showSignDialog(@NonNull Option approveOption) {
        closeDialog();
        var signPanel = approveOption == Option.SIGNIN ? signInPanel : signUpPanel;
        if (signPanel.showDialog(WelcomeDialog.this) == Option.OK) {
            setOption(approveOption);
        }
        else {
            showDialog(WelcomeDialog.this);
        }
    }

    public @NonNull User getSignInUser() {
        return signInPanel.getUser();
    }

    public @NonNull User getSingUpUser() {
        return signUpPanel.getUser();
    }

    @Override
    public @NonNull Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    @Override
    protected void reset() {
        setOption(Option.EXIT);
    }

    @Override
    protected void initiateDialog(Component parent) {
        super.initiateDialog(parent);
        setIconImage(Utils.getIcon(IconType.GENERAL));
        addWindowListener(Utils.exitOnClosing(null));
    }

    @Override
    protected @NonNull JButton getDefaultButton() {
        return signInButton;
    }

    @Override
    protected @NonNull String getTitle() {
        return "欢迎使用本系统";
    }
}
package ui.sub;


import org.jetbrains.annotations.NotNull;

public class AccountInfoPanel extends SignUpPanel {
    public AccountInfoPanel() {
        setEditableFor(ACCOUNT_COLUMN, false);
        setEditableFor(PASSWORD_COLUMN, false);
    }

    @Override
    public boolean checkIntegrity() {
        return haveText(ACCOUNT_COLUMN, PASSWORD_COLUMN);
    }

    @Override
    public @NotNull String getTitle() {
        return "修改账户信息";
    }
}

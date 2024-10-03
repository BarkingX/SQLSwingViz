package ui.abs;

import org.jetbrains.annotations.NotNull;
import ui.util.Utils;
import util.IconType;
import util.Option;
import util.StrMetadata;
import util.User;

import java.awt.*;

public abstract class SignPanel extends DataIODialogWrapper {
    @Override
    public void onOkOperation() {
        if (allHaveText(ACCOUNT_COLUMN, PASSWORD_COLUMN)) {
            setOption(Option.OK);
            closeDialog();
        }
        else {
            Utils.showErrorDialog(this, "请输入完整的账户名和密码", "错误");
        }
    }

    @Override
    protected void initiateDialog(Component parent) {
        super.initiateDialog(parent);
        setDialogIconImage(Utils.getIcon(IconType.GENERAL));
    }

    @Override
    public @NotNull StrMetadata getMetadata() {
        return User.METADATA;
    }

    public @NotNull User getUser() {
        return new User(getTextOf(ACCOUNT_COLUMN), getTextOf(PASSWORD_COLUMN));
    }
}

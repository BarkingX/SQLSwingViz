package ui.abs;

import org.jetbrains.annotations.NotNull;
import ui.util.IconSupplier;
import ui.util.Utils;
import util.IconType;
import util.Option;
import util.StrMetadata;
import util.User;

public abstract class SignPanel extends DataIODialogWrapper implements IconSupplier {
    public SignPanel() {
        replaceWithPasswordField(PASSWORD_COLUMN);
        setDialogIconImage(icons.get(IconType.GENERAL));
    }

    @Override
    public void onOkOperation() {
        if (checkIntegrity()) {
            setOption(Option.OK);
            closeDialog();
        }
        else {
            Utils.showErrorDialog(this, "请输入完整的账户名和密码", "错误");
        }
    }

    public boolean checkIntegrity() {
        return allHaveText(ACCOUNT_COLUMN, PASSWORD_COLUMN);
    }

    @Override
    public @NotNull StrMetadata getMetadata() {
        return User.METADATA;
    }

    public @NotNull User getUser() {
        return new User(getTextOf(ACCOUNT_COLUMN), getTextOf(PASSWORD_COLUMN));
    }
}

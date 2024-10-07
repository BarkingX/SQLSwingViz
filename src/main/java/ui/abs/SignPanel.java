package ui.abs;

import lombok.NonNull;
import ui.util.Utils;
import util.*;

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
        setIconImage(Utils.getIcon(IconType.GENERAL));
    }

    @Override
    public @NonNull Metadata<String> getMetadata() {
        return User.METADATA;
    }

    public @NonNull User getUser() {
        return new User(getTextOf(ACCOUNT_COLUMN), getTextOf(PASSWORD_COLUMN));
    }
}

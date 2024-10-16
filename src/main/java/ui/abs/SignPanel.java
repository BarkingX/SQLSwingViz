package ui.abs;

import lombok.NonNull;
import model.User;
import ui.util.IconType;
import ui.util.Option;
import ui.util.UiUtil;
import util.*;

import java.awt.*;

public abstract class SignPanel extends DataIODialogWrapper {
    @Override
    public void onOkOperation() {
        if (allHaveText(ACCOUNT_COLUMN, PASSWORD_COLUMN) && validateMinLength(2, 6)) {
            setOption(Option.OK);
            closeDialog();
        }
        else {
            UiUtil.showErrorDialog(this, "账号或密码不合规！", "错误");
        }
    }

    @Override
    protected void initiateDialog(Component parent) {
        super.initiateDialog(parent);
        setIconImage(UiUtil.getIcon(IconType.GENERAL));
    }

    @Override
    public @NonNull Metadata<String> getMetadata() {
        return User.METADATA;
    }

    public @NonNull User getUser() {
        return new User(getTextOf(ACCOUNT_COLUMN), getTextOf(PASSWORD_COLUMN));
    }

    public boolean validateMinLength(int minLengthA, int minLengthP) {
        return getTextOf(ACCOUNT_COLUMN).length() >= minLengthA
                && getTextOf(PASSWORD_COLUMN).length() >= minLengthP;
    }
}

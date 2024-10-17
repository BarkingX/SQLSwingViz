package ui.abs;

import lombok.NonNull;
import model.Role;
import model.User;
import ui.util.Option;
import ui.util.UiUtil;
import util.*;

import java.awt.*;

import static ui.util.IoTypeConstants.ACCOUNT;
import static ui.util.IoTypeConstants.PASSWORD;

public abstract class SignPanel extends DataIODialogWrapper {
    public SignPanel() {
        setPasswordField(PASSWORD);
    }

    @Override
    public @NonNull Metadata<String> getMetadata() {
        return User.METADATA;
    }

    @Override
    protected void initiateDialog(Component parent) {
        super.initiateDialog(parent);
        setIconImage(UiUtil.getIcon(Role.NULL));
    }

    @Override
    public void onOkOperation() {
        if (allHaveText(ACCOUNT, PASSWORD) && validateMinLength(2, 6)) {
            setOption(Option.OK);
            closeDialog();
        }
        else {
            UiUtil.showErrorDialog(this, getErrorMessage(), "错误");
        }
    }

    @NonNull
    public String getErrorMessage() {
        return "账号" + getTitle() + "失败，请检查以下原因：\n- 账号应由2至16位字母或数字、密码应由6至16位字母或数字组成";
    }

    public @NonNull User getUser() {
        return User.create(getTextOf(ACCOUNT), getTextOf(PASSWORD));
    }

    public boolean validateMinLength(int minLengthA, int minLengthP) {
        return getTextOf(ACCOUNT).length() >= minLengthA
                && getTextOf(PASSWORD).length() >= minLengthP;
    }
}

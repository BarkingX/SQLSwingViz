package ui.sub;

import lombok.NonNull;
import model.Role;
import model.User;
import ui.util.IoTypeConstants;
import ui.util.ComboBoxFilter;
import util.Metadata;
import util.StrMetadata;


import static util.FilterType.TYPE;

public class RootSignUpPanel extends SignUpPanel {
    private final ComboBoxFilter<Role> roleFilter = new ComboBoxFilter<>(TYPE, Role.roles());

    public RootSignUpPanel() {
        replaceWith(IoTypeConstants.ROLE, roleFilter);
    }

    @Override
    public @NonNull User getUser() {
        return super.getUser().withRole(getRole());
    }

    @Override
    public @NonNull Metadata<String> getMetadata() {
        var result = new StrMetadata(User.METADATA.toArray(new String[0]));
        result.add("身份");
        return result;
    }

    public @NonNull Role getRole() {
        return roleFilter.getValue();
    }
}

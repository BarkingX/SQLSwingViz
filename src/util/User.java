package util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class User implements MetadataSupplier<String> {
    public static final StrMetadata METADATA = new StrMetadata("账号", "密码");
    public final String account;
    public final String password;
    public User(String account, String password) {
        this.account = account;
        this.password = password;
    }

    @Override
    public @NotNull Metadata<String> getMetadata() {
        return METADATA;
    }

    public @NotNull List<String> toList() {
        return List.of(account, password);
    }
}
package model;

import lombok.*;
import util.Metadata;
import util.MetadataSupplier;
import util.StrMetadata;

import java.util.List;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User implements MetadataSupplier<String> {
    public static final StrMetadata METADATA = new StrMetadata("账号", "密码");
    public final String account;
    public final String password;
    public final String type;

    @Override
    public @NonNull Metadata<String> getMetadata() {
        return METADATA;
    }

    public @NonNull List<String> infos() {
        return List.of(account, password, type);
    }

    public @NonNull User withRole(@NonNull Role role) {
        return create(account, password, role);
    }

    public static @NonNull User create(@NonNull String account, @NonNull String password) {
        return create(account, password, Role.NULL);
    }

    public static @NonNull User create(@NonNull String account, @NonNull String password, @NonNull Role role) {
        return new User(account, password, role.name().toLowerCase());
    }
}
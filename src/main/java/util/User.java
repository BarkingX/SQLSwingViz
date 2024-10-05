package util;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
public class User implements MetadataSupplier<String> {
    public static final StrMetadata METADATA = new StrMetadata("账号", "密码");
    public final String account;
    public final String password;

    @Override
    public @NonNull Metadata<String> getMetadata() {
        return METADATA;
    }

    public @NonNull List<String> toList() {
        return List.of(account, password);
    }
}
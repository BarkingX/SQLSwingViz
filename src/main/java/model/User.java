package model;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import util.Metadata;
import util.MetadataSupplier;
import util.StrMetadata;

@AllArgsConstructor
public class User implements MetadataSupplier<String> {
    public static final StrMetadata METADATA = new StrMetadata("账号", "密码");
    public final String account;
    public final String password;

    @Override
    public @NonNull Metadata<String> getMetadata() {
        return METADATA;
    }
}
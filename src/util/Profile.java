package util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class Profile implements MetadataSupplier<String> {
    public static final StrMetadata METADATA = new StrMetadata(
            new StrMetadata("姓名", "电子邮件", "联系电话"), Address.METADATA);
    public final String name;
    public final String email;
    public final String phone;
    public final Address address;

    private Profile(String name, String email, String phone, Address address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static @NotNull Profile of(String name, String email, String phone, Address address) {
        return new Profile(name, email, phone, address);
    }

    public List<String> toList() {
        return Arrays.asList(name, email, phone,
                address.province, address.city, address.district,
                address.address);
    }

    @Override
    public Metadata<String> getMetadata() {
        return METADATA;
    }
}

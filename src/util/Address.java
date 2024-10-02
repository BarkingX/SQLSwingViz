package util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Address implements MetadataSupplier<String> {
    public static final StrMetadata METADATA = new StrMetadata("省", "市", "区", "地址");
    public final String province;
    public final String city;
    public final String district;
    public final String address;

    private Address(String province, String city, String district, String address) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.address = address;
    }

    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static @NotNull Address of(String province, String city,
                                      String district, String address) {
        return new Address(province, city, district, address);
    }

    @Override
    public Metadata<String> getMetadata() {
        return METADATA;
    }
}

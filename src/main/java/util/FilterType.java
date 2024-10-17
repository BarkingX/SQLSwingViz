package util;

import java.util.Set;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;


public enum FilterType {
    QUERY_TYPE, CITY_NAME, PORT_CODE, SEA_NAME, YEAR, TYPE;

    public static @NonNull Set<FilterType> basicFilters() {
        return ImmutableSet.of(CITY_NAME, PORT_CODE, SEA_NAME);
    }
}

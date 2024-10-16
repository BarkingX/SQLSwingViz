package util;

import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.*;

import static database.Query.*;
import static util.FilterType.*;

@AllArgsConstructor
public enum QueryType {
    RECORD("水域环境信息", CALL_SHOW_RECORD, basicFilters()),
    SAMPLE("水质信息", CALL_SHOW_SAMPLE, basicFilters()),
    PORT("港口信息", CALL_SHOW_PORT, basicFilters()),
    NO_STATION("没有监测点的港口", CALL_SHOW_NO_STATION, Collections.emptySet()),
    SAMPLE_STATISTICS("年平均水质", CALL_SHOW_SAMPLE_STATISTICS, Set.of(PORT_CODE, YEAR)),
    ANNUAL_REPORT("年度数据报告", CALL_SHOW_ANNUAL_REPORT, Set.of(PORT_CODE, YEAR)),
    USER("用户信息", SELECT_USER, Set.of(TYPE));

    @Getter public final String value;
    public final String sql;
    public final Set<FilterType> filters;

    private static final Map<String, QueryType> VALUE_TO_ENUM_MAP = Maps.uniqueIndex(Arrays.asList(QueryType.values()), QueryType::getValue);

    public static @NonNull Optional<QueryType> of(String value) {
        return Optional.ofNullable(VALUE_TO_ENUM_MAP.get(value));
    }
}

package util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collections;
import java.util.Set;

import static database.Query.*;
import static util.FilterType.*;

@AllArgsConstructor
public enum QueryType {
    RECORD("水域环境信息", CALL_SHOW_RECORD, basicFilters()),
    SAMPLE("水质信息", CALL_SHOW_SAMPLE, basicFilters()),
    PORT("港口信息", CALL_SHOW_PORT, basicFilters()),
    NO_STATION("没有监测点的港口", CALL_SHOW_NO_STATION, Collections.emptySet()),
    SAMPLE_STATISTICS("年平均水质", CALL_SHOW_SAMPLE_STATISTICS, Set.of(PORT_CODE, YEAR)),
    ANNUAL_REPORT("年度数据报告", CALL_SHOW_ANNUAL_REPORT, Set.of(PORT_CODE, YEAR));

    @Getter public final String value;
    public final String sql;
    public final Set<FilterType> filters;

    public static @NonNull QueryType of(String value) {
        return RECORD.value.equals(value) ?
                RECORD : (SAMPLE.value.equals(value) ?
                SAMPLE : (PORT.value.equals(value) ?
                PORT : (NO_STATION.value.equals(value) ?
                NO_STATION : (SAMPLE_STATISTICS.value.equals(value) ?
                SAMPLE_STATISTICS : ANNUAL_REPORT))));
    }
}

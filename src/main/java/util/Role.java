package util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.LinkedHashSet;
import java.util.Set;
import static util.QueryType.*;


@Getter
@AllArgsConstructor
public enum Role {
    ADMIN(queriesOf(RECORD, SAMPLE, PORT)),
    OFFICIAL(queriesOf(RECORD, SAMPLE, NO_STATION, SAMPLE_STATISTICS, ANNUAL_REPORT)),
    USER(queriesOf(RECORD, SAMPLE)),
    //TODO add ROOT queries
    ROOT(queriesOf(RECORD, SAMPLE)),
    UNKNOWN(Set.of());

    private final Set<QueryType> authorizedQueries;

    private static @NonNull Set<QueryType> queriesOf(@NonNull QueryType... queries) {
        return new LinkedHashSet<>(Set.of(queries));
    }
}

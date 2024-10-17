package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import util.QueryType;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import static util.QueryType.*;


@Getter
@AllArgsConstructor
public enum Role {
    ADMIN(queriesOf(RECORD, SAMPLE, PORT)),
    OFFICIAL(queriesOf(RECORD, SAMPLE, NO_STATION, SAMPLE_STATISTICS, ANNUAL_REPORT)),
    USER(queriesOf(RECORD, SAMPLE)),
    NONE(queriesOf(RECORD, SAMPLE, QueryType.USER)),
    NULL(Collections.emptySet());

    private final Set<QueryType> authorizedQueries;

    private static @NonNull Set<QueryType> queriesOf(@NonNull QueryType... queries) {
        return new LinkedHashSet<>(Set.of(queries));
    }

    public static @NonNull Set<Role> roles() {
        return Set.of(ADMIN, OFFICIAL, USER);
    }

    public static @NonNull Role of(@NonNull String name) {
        return Role.valueOf(name.toUpperCase());
    }
}

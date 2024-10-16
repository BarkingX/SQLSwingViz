package database;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DatabaseUtil {
    private DatabaseUtil() {}

    @SneakyThrows
    static @NonNull Collection<String> selectAndReturnCollection(@NonNull Connection conn,
                                                                 @NonNull String query) {
        return column(conn.createStatement().executeQuery(query), 1);
    }

    static @NonNull Collection<String> column(@NonNull ResultSet resultSet, int columnIndex) {
        try (resultSet) {
            var result = new ArrayList<String>();
            while (resultSet.next()) {
                result.add(resultSet.getString(columnIndex));
            }
            return result;
        }
        catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    static boolean execute(@NonNull Connection conn, @NonNull String query,
                           @NonNull List<String> args) throws SQLException{
        return prepared(conn, query, args).execute();
    }

    static ResultSet executeQuery(@NonNull Connection conn, String query,
                                   @NonNull List<String> args) throws SQLException {
        var stat = prepared(conn, query, args);
        stat.execute();
        return stat.getResultSet();
    }

    static int executeUpdate(@NonNull Connection conn, String query,
                              @NonNull List<String> args) throws SQLException {
        return prepared(conn, query, args).executeUpdate();
    }

    static @NonNull PreparedStatement prepared(@NonNull Connection conn, String query,
                                               @NonNull List<String> args) throws SQLException {
        var stat = conn.prepareStatement(query);
        for (int i = 0; i < args.size(); i++) {
            if (args.get(i) != null && !args.get(i).isEmpty()) {
                stat.setString(i + 1, args.get(i));
            }
            else {
                stat.setNull(i + 1, Types.NULL);
            }
        }
        return stat;
    }

    static @NonNull Collection<String> pastNYears(int nYears) {
        int currentYear = LocalDateTime.now().getYear();
        return IntStream.rangeClosed(currentYear - nYears - 1, currentYear)
                .mapToObj(String::valueOf)
                .collect(Collectors.toUnmodifiableList());
    }
}

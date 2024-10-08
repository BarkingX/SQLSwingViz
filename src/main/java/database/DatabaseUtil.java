package database;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    static ResultSet executeQuery(@NonNull Connection conn, String query,
                                   @NonNull List<String> parameters) throws SQLException {
        var stat = prepared(conn, query, parameters);
        stat.execute();
        return stat.getResultSet();
    }

    static int executeUpdate(@NonNull Connection conn, String query,
                              @NonNull List<String> parameters) throws SQLException {
        return prepared(conn, query, parameters).executeUpdate();
    }

    static @NonNull PreparedStatement prepared(@NonNull Connection conn, String query,
                                               @NonNull List<String> parameters) throws SQLException {
        var stat = conn.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i) != null && !parameters.get(i).isEmpty()) {
                stat.setString(i + 1, parameters.get(i));
            }
            else {
                stat.setNull(i + 1, Types.NULL);
            }
        }
        return stat;
    }
}

package database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Strings.isNullOrEmpty;

@NoArgsConstructor(access = AccessLevel.NONE)
public class DatabaseUtil {
    private static final String CONFIGURATION = "db.properties";
    private static final Properties properties = new Properties();

    static {
        try (var f = DatabaseUtil.class.getClassLoader().getResourceAsStream(CONFIGURATION)) {
            properties.load(Objects.requireNonNull(f, "Unable to find " + CONFIGURATION));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static @NonNull Connection getConnection() throws SQLException {
        return getConnection(properties.getProperty("db.username"),
                             properties.getProperty("db.password"));
    }

    static @NonNull Connection getConnection(@NonNull String username, @NonNull String password)
            throws SQLException {
        var url = properties.getProperty("db.url");
        assert !(isNullOrEmpty(url) || isNullOrEmpty(username) || isNullOrEmpty(password));
        return DriverManager.getConnection(url, username, password);
    }

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

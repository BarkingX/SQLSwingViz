package database;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import model.*;
import util.*;

import javax.sql.rowset.RowSetProvider;
import javax.swing.table.TableModel;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.FluentIterable.from;
import static database.DatabaseUtil.*;
import static database.Query.*;
import static util.FilterType.*;


public class Database implements IDatabase {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/port";
    private final UnassignedFilterMap<String> metaFilters = new UnassignedFilterMap<>();
    private final Connection root;
    private Connection user;

    public Database() throws SQLException {
        root = DriverManager.getConnection(URL, "root", "527310");
        populateFilterMap();
    }

    private void populateFilterMap() {
        loadFilter(YEAR, pastNYears(10));
        loadFilter(CITY_NAME, selectAndReturnCollection(root, SELECT_CITY_NAME));
        loadFilter(PORT_CODE, selectAndReturnCollection(root, SELECT_PORT_CODE));
        loadFilter(SEA_NAME, selectAndReturnCollection(root, SELECT_SEA));
    }

    private void loadFilter(@NonNull FilterType filterType,
                            @NonNull Collection<String> filter) {
        metaFilters.put(filterType, new LinkedHashSet<>(Sets.union(Set.of(""), Set.copyOf(filter))));
    }

    @Override
    public @NonNull Collection<String> getTableNames() {
        return List.of("port", "monitor", "station", "record", "sample");
    }

    @Override
    public void authenticate(@NonNull User user) throws SQLException {
        this.user = DriverManager.getConnection(URL, user.account, user.password);
    }

    @SneakyThrows
    private @NonNull Role selectRole() {
        @Cleanup var rs = executeQuery(this.user, SELECT_ROLE, Collections.emptyList());
        rs.next();
        return Role.of(from(Splitter.on("`").omitEmptyStrings().split(rs.getString(1)))
                       .first().or(Role.USER.name()));
    }

    @SneakyThrows
    @Override
    public void disconnect() {
        if (!isDisconnected()) {
            user.close();
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        root.close();
        disconnect();
    }

    @Override
    public void register(@NonNull User user) throws Exception {
        boolean autoCommit = root.getAutoCommit();
        try {
            root.setAutoCommit(false);
            executeUpdate(root, CREATE_USER, List.of(user.account, user.password));
            executeUpdate(root, INSERT_USER, List.of(user.account, user.password));
            root.commit();
        }
        catch (SQLException e) {
            root.rollback();
            throw new RuntimeException(e);
        }
        finally {
            root.setAutoCommit(autoCommit);
        }
    }

    @Override
    public void loadDataInfile(@NonNull String path, @NonNull String tableName) {
        try {
            executeUpdate(root, String.format(LOAD_DATA_INFILE, tableName), List.of(path));
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NonNull Optional<TableModel> queryWithFilter(@NonNull String mainFilter,
                                                         @NonNull AssignedFilterMap<String> assignedFilterMap) {
        var queryType = QueryType.of(mainFilter).orElseThrow();
        try (var rs = executeQuery(user, queryType.sql, assignedFilterMap.gets(queryType.filters))) {
            var crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            crs.setTableName(queryType.name());
            return Optional.of(new QueryModel(crs));
        }
        catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteFromWheres(@NonNull String tableName,
                                 @NonNull Collection<Map<String, Object>> wheres) throws SQLException {
        if (wheres.isEmpty()) {
            throw new RuntimeException("请选择需要删除的记录");
        }

        boolean autoCommit = user.getAutoCommit();
        try {
            user.setAutoCommit(false);
            execute(user, TURN_OFF_SAFE_UPDATES, Collections.emptyList());
            for (var where : wheres) {
                executeUpdate(user, CALL_DELETE_FROM_WHERES, ImmutableList.<String>builder()
                        .add(tableName)
                        .addAll(where.entrySet().stream()
                                .flatMap(entry -> Stream.of(entry.getKey(), String.valueOf(entry.getValue())))
                                .iterator()).build());
            }
            user.commit();
        }
        catch (SQLException e) {
            user.rollback();
            throw new RuntimeException(e);
        }
        finally {
            execute(user, TURN_ON_SAFE_UPDATES, Collections.emptyList());
            user.setAutoCommit(autoCommit);
        }
    }

    @Override
    public @NonNull UnassignedFilterMap<String> getQueryTypeFilterMap() {
        return new UnassignedFilterMap<>(QUERY_TYPE,
                selectRole().getAuthorizedQueries().stream()
                            .map(QueryType::getValue)
                            .collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public @NonNull UnassignedFilterMap<String> getFilterMap(@NonNull String target) {
        return new UnassignedFilterMap<>(
                QueryType.of(target)
                         .orElseThrow()
                         .filters.stream()
                         .collect(Collectors.toMap(Function.identity(), metaFilters::get)));
    }

    @Override
    public boolean isDisconnected() {
        try {
            return user == null || user.isClosed();
        }
        catch (SQLException e) {
            return true;
        }
    }

    @Override
    public boolean hasPrivilegeOfImportingData() {
        return selectRole().equals(Role.ADMIN);
    }
}

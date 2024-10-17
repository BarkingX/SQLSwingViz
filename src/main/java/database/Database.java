package database;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import model.QueryModel;
import model.Role;
import model.User;
import util.AssignedFilterMap;
import util.FilterType;
import util.QueryType;
import util.UnassignedFilterMap;

import javax.sql.rowset.RowSetProvider;
import javax.swing.table.TableModel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Sets.union;
import static database.DatabaseUtil.*;
import static database.Query.*;
import static util.FilterType.*;


public class Database implements IDatabase {
    private final UnassignedFilterMap<String> metaFilters = new UnassignedFilterMap<>();
    private final Connection root;
    private Connection user;

    public Database() throws SQLException {
        root = DatabaseUtil.getConnection();
        populateFilterMap();
    }

    private void populateFilterMap() {
        loadFilter(YEAR, pastNYears(10), "");
        loadFilter(CITY_NAME, selectAndReturnCollection(root, SELECT_CITY_NAME), "");
        loadFilter(PORT_CODE, selectAndReturnCollection(root, SELECT_PORT_CODE), "");
        loadFilter(SEA_NAME, selectAndReturnCollection(root, SELECT_SEA), "");
        loadFilter(TYPE, selectAndReturnCollection(root, SELECT_ROLE), "%");
    }

    private void loadFilter(@NonNull FilterType filterType,
                            @NonNull Collection<String> filter,
                            @NonNull String empty) {
        metaFilters.put(filterType, new LinkedHashSet<>(union(Set.of(empty), Set.copyOf(filter))));
    }

    @Override
    public @NonNull Collection<String> getTableNames() {
        return List.of("port", "monitor", "station", "record", "sample");
    }

    @Override
    public @NonNull Role authenticate(@NonNull User user) throws SQLException {
        this.user = getConnection(user.account, user.password);
        return selectRole();
    }

    @SneakyThrows
    private @NonNull Role selectRole() {
        @Cleanup var rs = executeQuery(user, CURRENT_ROLE, Collections.emptyList());
        rs.next();
        return Role.of(from(Splitter.on("`").omitEmptyStrings().split(rs.getString(1))).first().or(Role.USER.name()));
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
            executeUpdate(root, CREATE_USER, user.infos());
            executeUpdate(root, INSERT_USER, user.infos());
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
                executeUpdate(user, CALL_DELETE_FROM_WHERES, parasForDeleting(tableName, where));
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

    private @NonNull List<String> parasForDeleting(@NonNull String tableName,
                                                   @NonNull Map<String, Object> where) {
        return ImmutableList.<String>builder()
                .add(tableName)
                .addAll(where.entrySet().stream()
                        .flatMap(entry -> Stream.of(entry.getKey(), String.valueOf(entry.getValue())))
                        .iterator()).build();
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
    public boolean hasPrivilegeOfImportingData(@NonNull User user) {
        return Role.ADMIN.equals(Role.of(user.type));
    }

    @Override
    public boolean hasPrivilegeOfRegisteringUser(@NonNull User user) {
        return Role.NONE.equals(Role.of(user.type));
    }
}

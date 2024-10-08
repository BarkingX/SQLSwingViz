package database;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import util.*;

import javax.sql.rowset.RowSetProvider;
import javax.swing.table.TableModel;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        loadFilter(YEAR, Set.of("2021", "2020", "2019", "2018", "2017", "2016"));
        loadFilter(CITY_NAME, selectAndReturnCollection(root, SELECT_CITY_NAME));
        loadFilter(PORT_CODE, selectAndReturnCollection(root, SELECT_PORT_CODE));
        loadFilter(SEA_NAME, selectAndReturnCollection(root, SELECT_SEA_NAME));
    }

    private void loadFilter(@NonNull FilterType filterType,
                            @NonNull Collection<String> filter) {
        metaFilters.put(filterType, new LinkedHashSet<>(Sets.union(Set.of(""), Set.copyOf(filter))));
    }

    @SneakyThrows
    @Override
    public @NonNull Collection<String> getTableNames() {
        var result = column(root.getMetaData().getTables("port", null, null, new String[] { "TABLE" }),
                3);

        if (!selectRole().equals(Role.NONE)) {
            result.remove("user");
        }
        return result;
//        return column(root.getMetaData().getTables("port", null, null, new String[] { "TABLE" }),
//                3);
    }

    @SneakyThrows
    @Override
    public void authenticate(@NonNull User user) {
        this.user = DriverManager.getConnection(URL, user.account, user.password);
    }

    @SneakyThrows
    private @NonNull Role selectRole() {
        @Cleanup
        var rs = executeQuery(this.user, SELECT_ROLE, Collections.emptyList());
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

    //TODO validate
    @Override
    public void register(@NonNull User user) throws Exception {
        boolean autoCommit = root.getAutoCommit();
        try {
            root.setAutoCommit(false);
            executeUpdate(root, CREATE_USER, user.toList());
            executeUpdate(root, INSERT_USER, user.toList());
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
    public void loadDataInfile(@NonNull String path, String tableName) {
        try {
            executeUpdate(root, String.format(LOAD_DATA_INFILE, tableName), List.of(path));
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<TableModel> queryWithFilter(@NonNull FilterWrapper<String> mainFilter,
                                                @NonNull AssignedFilterMap<String> assignedFilterMap) {
        var queryType = QueryType.of(mainFilter.getValue());
        try (var rs = executeQuery(user, queryType.sql, assignedFilterMap.gets(queryType.filters))) {
            var crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            return Optional.of(new QueryModel(crs));
        }
        catch (SQLException e) {
            return Optional.empty();
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

package database;

import lombok.NonNull;
import lombok.SneakyThrows;
import util.*;

import javax.sql.rowset.RowSetProvider;
import javax.swing.table.TableModel;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static database.Query.*;
import static util.FilterType.*;


public class Database implements IDatabase {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/port";
    private final UnassignedFilterMap<String> metaFilters = new UnassignedFilterMap<>();
    private final Connection root;
    private PreparedStatement stat;
    private Connection user;

    public Database() throws SQLException {
        root = DriverManager.getConnection(URL, "root", "527310");
        populateFilterMap();
    }

    private void populateFilterMap() {
        loadFilter(YEAR, new ArrayList<>(List.of("2021", "2020", "2019", "2018", "2017", "2016")));
        loadFilter(CITY_NAME, selectAndReturnCollection(root, SELECT_CITY_NAME));
        loadFilter(PORT_CODE, selectAndReturnCollection(root, SELECT_PORT_CODE));
        loadFilter(SEA_NAME, selectAndReturnCollection(root, SELECT_SEA_NAME));
    }

    private void loadFilter(@NonNull FilterType filterType,
                            @NonNull Collection<String> filter) {
        filter.add(null);
        metaFilters.put(filterType, new LinkedHashSet<>(filter));
    }

    private @NonNull Collection<String> selectAndReturnCollection(Connection conn, String query) {
        try (var stat = conn.createStatement();
             var rs = stat.executeQuery(query)) {
            var result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public @NonNull Collection<String> getTableNames() {
        try (var rs = root.getMetaData().getTables("port", null,
                null, new String[] { "TABLE" })) {
            var tableNames = new LinkedList<String>();
            while (rs.next()) {
                tableNames.add(rs.getString(3));
            }
            return tableNames;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @SneakyThrows
    @Override
    public void authenticate(@NonNull User user) {
        this.user = DriverManager.getConnection(URL, user.account, user.password);
    }

    private @NonNull Role selectRole() {
        try (var rs = executeQuery(this.user, SELECT_ROLE, Collections.emptyList())) {
            return rs.next() ? Role.valueOf(rs.getString(1).split("`")[1].toUpperCase())
                             : Role.ROOT;
        }
        catch (SQLException | IllegalArgumentException e) {
            return Role.UNKNOWN;
        }
    }

    private ResultSet executeQuery(Connection conn, String query, List<String> parameters) throws SQLException {
        execute(conn, query, parameters);
        return stat.getResultSet();
    }

    private void execute(Connection conn, String query, List<String> parameters) throws SQLException {
        prepare(conn, query, parameters);
        stat.execute();
    }

    private void prepare(@NonNull Connection conn, String query,
                         @NonNull List<String> parameters) throws SQLException {
        stat = conn.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i) != null) {
                stat.setString(i + 1, parameters.get(i));
            }
            else {
                stat.setNull(i + 1, Types.NULL);
            }
        }
    }

    @SneakyThrows
    @Override
    public void disconnect() {
        if (user != null && !user.isClosed()) {
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            root.setAutoCommit(autoCommit);
        }
    }

    private void executeUpdate(Connection conn, String query, List<String> parameters) throws SQLException {
        execute(conn, query, parameters);
        stat.getUpdateCount();
    }

    @Override
    public void loadDataInfile(@NonNull String path, String tableName) {
        try {
            var filePath = '\'' + path.replaceAll("\\\\", "/") + '\'';
            var query = LOAD_DATA_INFILE.replaceFirst("\\?", filePath).replace("?", tableName);
            executeUpdate(root, query, Collections.emptyList());
        }
        catch (SQLException e) {
            e.printStackTrace();
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
    public boolean isClosed() {
        try {
            return user == null || user.isClosed();
        }
        catch (SQLException e) {
            return true;
        }
    }

    @Override
    public boolean hasPrivilegeOfImportingData() {
        var role = selectRole();
        return role.equals(Role.ROOT) || role.equals(Role.ADMIN);
    }
}

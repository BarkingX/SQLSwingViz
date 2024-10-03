package database;

import org.jetbrains.annotations.NotNull;
import util.*;

import javax.sql.rowset.RowSetProvider;
import javax.swing.table.TableModel;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static database.Query.*;
import static util.FilterType.*;


public class Database implements IDatabase {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/port";
    private final UnassignedFilterMap<String> filterMap = new UnassignedFilterMap<>();
    private final Connection root;
    private PreparedStatement stat;
    private Connection user;

    public Database() throws SQLException {
        root = DriverManager.getConnection(URL, "root", "527310");
        populateFilterMap();
    }

    private void populateFilterMap() {
        final var yearFilter = new LinkedHashSet<>(Set.of("2021", "2020", "2019", "2018", "2017", "2016"));
        yearFilter.add(null);
        loadFilter(YEAR, () -> yearFilter);
        loadFilter(CITY_NAME, () -> selectAndReturnCollection(root, SELECT_CITY_NAME));
        loadFilter(PORT_CODE, () -> selectAndReturnCollection(root, SELECT_PORT_CODE));
        loadFilter(SEA_NAME, () -> selectAndReturnCollection(root, SELECT_SEA_NAME));
    }

    private void loadFilter(FilterType filterType,
                            @NotNull Supplier<Collection<String>> filterSupplier) {
        var filter = filterSupplier.get();
        filter.add(null);
        filterMap.put(filterType, new LinkedHashSet<>(filter));
    }

    public @NotNull Collection<String> selectAndReturnCollection(Connection conn, String query) {
        try (var stat = conn.createStatement();
             var rs = stat.executeQuery(query)) {
            var result = new LinkedHashSet<String>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        }
        catch (SQLException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public @NotNull Collection<String> getTableNames() {
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

    @Override
    public void authenticate(@NotNull User user) {
        try {
            this.user = DriverManager.getConnection(URL, user.account, user.password);
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull Role selectRole() {
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

    private void prepare(@NotNull Connection conn, String query,
                         @NotNull List<String> parameters) throws SQLException {
        stat = conn.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            var p = parameters.get(i);
            if (p != null) {
                stat.setString(i + 1, p);
            }
            else {
                stat.setNull(i + 1, Types.NULL);
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            user.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            root.close();
            if (user != null && !user.isClosed()) {
                disconnect();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //TODO validate
    @Override
    public void register(@NotNull User user) throws Exception {
        boolean autoCommit = root.getAutoCommit();
        try {
            root.setAutoCommit(false);
            executeUpdate(root, CREATE_USER, user.toList());
            executeUpdate(root, INSERT_USER_INFO, user.toList());
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
    public void loadDataInfile(@NotNull String path, String tableName) {
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
    public Optional<TableModel> queryWithFilter(@NotNull FilterWrapper<String> mainFilter,
                                                @NotNull AssignedFilterMap<String> assignedFilterMap) {
        var query = configureQuery(QueryType.ofValue(mainFilter.getValue()));
        var parameters = configureParameters(QueryType.ofValue(mainFilter.getValue()), assignedFilterMap);

        try (var rs = executeQuery(user, query, parameters)) {
            var crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(rs);
            return Optional.of(new QueryModel(crs));
        }
        catch (SQLException e) {
            return Optional.empty();
        }
    }

    private @NotNull String configureQuery(QueryType queryType) {
        return QueryType.RECORD.equals(queryType) ?
                CALL_SHOW_RECORD : (QueryType.SAMPLE.equals(queryType) ?
                CALL_SHOW_SAMPLE : (QueryType.PORT.equals(queryType) ?
                CALL_SHOW_PORT : (QueryType.NO_STATION.equals(queryType) ?
                CALL_SHOW_NO_STATION : (QueryType.SAMPLE_STATISTICS.equals(queryType) ?
                CALL_SHOW_SAMPLE_STATISTICS : CALL_SHOW_ANNUAL_REPORT))));
    }
    private @NotNull List<String> configureParameters(QueryType queryType,
                                                      @NotNull AssignedFilterMap<String> filterMap) {
        var parameters = new ArrayList<String>();

        if (QueryType.RECORD.equals(queryType)
                || QueryType.SAMPLE.equals(queryType)
                || QueryType.PORT.equals(queryType)) {
            var city = filterMap.get(CITY_NAME);
            var sea = filterMap.get(SEA_NAME);
            parameters.addAll(Arrays.asList(city, filterMap.get(PORT_CODE), sea));
        }
        else if (QueryType.SAMPLE_STATISTICS.equals(queryType)) {
            parameters.add(filterMap.get(PORT_CODE));
        }
        else if (QueryType.ANNUAL_REPORT.equals(queryType)){
            parameters.addAll(Arrays.asList(filterMap.get(PORT_CODE), filterMap.get(YEAR)));
        }
        return parameters;
    }

    @Override
    public @NotNull UnassignedFilterMap<String> getQueryTypeFilterMap() {
        var role = selectRole();
        var queryType = new LinkedHashSet<>(Set.of(QueryType.RECORD.value, QueryType.SAMPLE.value));
        if (role.equals(Role.ADMIN)) {
            queryType.add(QueryType.PORT.value);
        }
        else if (role.equals(Role.OFFICIAL)) {
            queryType.addAll(Set.of(QueryType.NO_STATION.value, QueryType.SAMPLE_STATISTICS.value,
                    QueryType.ANNUAL_REPORT.value));
        }
        return new UnassignedFilterMap<>(QUERY_TYPE, queryType);
    }

    @Override
    public @NotNull UnassignedFilterMap<String> getFilterMap(@NotNull FilterWrapper<String> mainFilter) {
        var filterMap = new UnassignedFilterMap<String>();
        var queryType = QueryType.ofValue(mainFilter.getValue());
        if (QueryType.RECORD.equals(queryType)
                || QueryType.SAMPLE.equals(queryType)
                || QueryType.PORT.equals(queryType)) {
            loadFilter(CITY_NAME, filterMap::put);
            loadFilter(PORT_CODE, filterMap::put);
            loadFilter(SEA_NAME, filterMap::put);
        }
        else if (QueryType.SAMPLE_STATISTICS.equals(queryType)){
            loadFilter(PORT_CODE, filterMap::put);
        }
        else if (QueryType.ANNUAL_REPORT.equals(queryType)) {
            loadFilter(PORT_CODE, filterMap::put);
            loadFilter(YEAR, filterMap::put);
        }
        return filterMap;
    }

    private void loadFilter(@NotNull FilterType filterType,
                            @NotNull BiConsumer<FilterType, Set<String>> mapLoader) {
        mapLoader.accept(filterType, filterMap.get(filterType));
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

    private enum Role {
        ADMIN, OFFICIAL, USER, ROOT, UNKNOWN
    }

    private enum QueryType {
        RECORD("水域环境信息"), SAMPLE("水质信息"), PORT("港口信息"),
        NO_STATION("没有监测点的港口"), SAMPLE_STATISTICS("年平均水质"),
        ANNUAL_REPORT("年度数据报告");
        private final String value;

        QueryType(String value) {
            this.value = value;
        }

        public static @NotNull QueryType ofValue(String value) {
            return RECORD.value.equals(value) ?
                    RECORD : (SAMPLE.value.equals(value) ?
                    SAMPLE : (PORT.value.equals(value) ?
                    PORT : (NO_STATION.value.equals(value) ?
                    NO_STATION : (SAMPLE_STATISTICS.value.equals(value) ?
                    SAMPLE_STATISTICS : ANNUAL_REPORT))));
        }
    }
}

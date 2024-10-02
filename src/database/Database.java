package database;

import org.jetbrains.annotations.NotNull;
import util.*;

import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static database.Query.*;
import static util.FilterType.*;

public class Database implements IDatabase {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/port";
    private final DatabaseMetaData databaseMetaData;
    private final UnassignedFilterMap<String> filterMap;
    private final RowSetFactory factory;
    private final Connection root;
    private PreparedStatement stat;
    private Connection user;
    private Role role;
    private boolean isConnected;

    public Database() throws SQLException {
        root = DriverManager.getConnection(URL, "root", "527310");
        factory = RowSetProvider.newFactory();
        databaseMetaData = root.getMetaData();
        filterMap = new UnassignedFilterMap<>();
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

    private void loadFilter(FilterType filterType, Supplier<Collection<String>> filterSupplier) {
        var filter = filterSupplier.get();
        filter.add(null);
        filterMap.put(filterType, filter);
    }

    public Collection<String> selectAndReturnCollection(Connection conn, String query) {
        var result = new LinkedHashSet<String>();
        try (var stat = conn.createStatement();
             var rs = stat.executeQuery(query)) {
            while (rs.next()) result.add(rs.getString(1));
        }
        catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    @Override
    public @NotNull Collection<String> getTableNames() {
        try (var rs = databaseMetaData.getTables("port", null,
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
            isConnected = true;
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void authorize(@NotNull String account) {
        if (account.equals("root")) role = Role.ROOT;
        else {
            try (var rs = executeQuery(root, SELECT_ROLE, List.of(account))) {
                role = rs.next() ? Role.valueOf(rs.getString("type").toUpperCase()) : Role.UNKNOWN;
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
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

    private void prepare(Connection conn, String query, List<String> parameters) throws SQLException {
        stat = conn.prepareStatement(query);
        for (int i = 0; i < parameters.size(); i++) {
            var p = parameters.get(i);
            if (!(p == null)) {
                stat.setString(i + 1, p);
            }
            else {
                stat.setNull(i + 1, Types.NULL);
            }
        }
    }

    @Override
    public void disconnect() {
        isConnected = false;
    }

    @Override
    public void close() {
        try {
            root.close();
            if (user != null && !user.isClosed()) user.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(List<String> userInfo, List<String> shippingInfo) throws Exception {
        boolean autoCommit = root.getAutoCommit();
        try {
            root.setAutoCommit(false);
            executeUpdate(root, CREATE_USER, userInfo);
            executeUpdate(root, INSERT_USER_INFO, userInfo);
            executeUpdate(root, INSERT_SHIPPING_INFO, shippingInfo);
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
    public void insertShippingInfo(List<String> shippingInfo) {
        try {
            executeUpdate(root, INSERT_SHIPPING_INFO, shippingInfo);
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void loadDataInfile(String path, String tableName) {
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
    public QueryModel queryWithFilter(FilterWrapper<String> mainFilter,
                                      AssignedFilterMap<String> assignedFilterMap) {
        var queryType = QueryType.ofValue(mainFilter.getValue());
        var query = configureQuery(queryType);
        var parameters = configureParameters(queryType, assignedFilterMap);

        try (var rs = executeQuery(user, query, parameters)) {
            var crs = factory.createCachedRowSet();
            crs.populate(rs);
            return new QueryModel(crs);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String configureQuery(QueryType queryType) {
        return QueryType.RECORD.equals(queryType) ?
                CALL_SHOW_RECORD : (QueryType.SAMPLE.equals(queryType) ?
                CALL_SHOW_SAMPLE : (QueryType.PORT.equals(queryType) ?
                CALL_SHOW_PORT : (QueryType.NO_STATION.equals(queryType) ?
                CALL_SHOW_NO_STATION : (QueryType.SAMPLE_STATISTICS.equals(queryType) ?
                CALL_SHOW_SAMPLE_STATISTICS : CALL_SHOW_ANNUAL_REPORT))));
    }
    private List<String> configureParameters(QueryType queryType,
                                             AssignedFilterMap<String> assignedFilterMap) {
        var parameters = new ArrayList<String>();

        var port = assignedFilterMap.get(PORT_CODE);
        if (QueryType.RECORD.equals(queryType) || QueryType.SAMPLE.equals(queryType)
                || QueryType.PORT.equals(queryType)) {
            var city = assignedFilterMap.get(CITY_NAME);
            var sea = assignedFilterMap.get(SEA_NAME);
            parameters.addAll(Arrays.asList(city, port, sea));
        }
        else if (QueryType.SAMPLE_STATISTICS.equals(queryType))
            parameters.add(port);
        else if (QueryType.ANNUAL_REPORT.equals(queryType)){
            var year = assignedFilterMap.get(YEAR);
            parameters.addAll(Arrays.asList(port, year));
        }
        return parameters;
    }

    public QueryModel selectShippingInfo() {
        try (var stat = root.createStatement();
             var rs = stat.executeQuery(SELECT_SHIPPING_INFO)) {
            var crs = factory.createCachedRowSet();
            crs.populate(rs);
            return new QueryModel(crs);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UnassignedFilterMap<String> getQueryTypeFilterMap() {
        var queryType = new LinkedHashSet<>(Set.of(QueryType.RECORD.value, QueryType.SAMPLE.value));
        if (role.equals(Role.ADMIN))
            queryType.add(QueryType.PORT.value);
        else if (role.equals(Role.OFFICIAL))
            queryType.addAll(Set.of(QueryType.NO_STATION.value, QueryType.SAMPLE_STATISTICS.value,
                                    QueryType.ANNUAL_REPORT.value));
        return new UnassignedFilterMap<>(QUERY_TYPE, queryType);
    }

    @Override
    public UnassignedFilterMap<String> getFilterMap(@NotNull FilterWrapper<String> mainFilter) {
        var filterMap = new UnassignedFilterMap<String>();
        var queryType = QueryType.ofValue(mainFilter.getValue());
        if (QueryType.RECORD.equals(queryType) || QueryType.SAMPLE.equals(queryType)
                || QueryType.PORT.equals(queryType)) {
            loadFilter(CITY_NAME, filterMap::put);
            loadFilter(PORT_CODE, filterMap::put);
            loadFilter(SEA_NAME, filterMap::put);
        }
        else if (QueryType.SAMPLE_STATISTICS.equals(queryType))
            loadFilter(PORT_CODE, filterMap::put);
        else if (QueryType.ANNUAL_REPORT.equals(queryType)) {
            loadFilter(PORT_CODE, filterMap::put);
            loadFilter(YEAR, filterMap::put);
        }
        return filterMap;
    }

    private void loadFilter(@NotNull FilterType filterType,
                            @NotNull BiConsumer<FilterType, LinkedHashSet<String>> mapLoader) {
        mapLoader.accept(filterType, filterMap.get(filterType));
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean hasPrivilegeOfImportingData() {
        return role.equals(Role.ROOT) || role.equals(Role.ADMIN);
    }

    @Override
    public boolean hasPrivilegeOfUserManagement() {
        return role.equals(Role.ROOT);
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

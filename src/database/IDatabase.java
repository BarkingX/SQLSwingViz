package database;

import util.*;

import java.util.Collection;
import java.util.List;

public interface IDatabase {
    Collection<String> getTableNames();
    UnassignedFilterMap<String> getQueryTypeFilterMap();
    UnassignedFilterMap<String> getFilterMap(FilterWrapper<String> mainFilter);
    void authenticate(User user);
    void authorize(String account);
    void disconnect();
    void close();
    void register(List<String> userInfo) throws Exception;
    void loadDataInfile(String path, String tableName);
    QueryModel queryWithFilter(FilterWrapper<String> mainFilter, AssignedFilterMap<String> assignedFilterMap);
    boolean isConnected();
    boolean hasPrivilegeOfImportingData();
    boolean hasPrivilegeOfUserManagement();
}

package database;

import lombok.NonNull;
import util.*;

import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.Optional;

public interface IDatabase {
    @NonNull Collection<String> getTableNames();
    @NonNull UnassignedFilterMap<String> getQueryTypeFilterMap();
    @NonNull UnassignedFilterMap<String> getFilterMap(FilterWrapper<String> mainFilter);
    void authenticate(User user);
    void disconnect();
    void close();
    void register(User user) throws Exception;
    void loadDataInfile(String path, String tableName);
    Optional<TableModel> queryWithFilter(FilterWrapper<String> mainFilter,
                                         AssignedFilterMap<String> assignedFilterMap);
    boolean isClosed();
    boolean hasPrivilegeOfImportingData();
}

package database;

import org.jetbrains.annotations.NotNull;
import util.*;

import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.Optional;

public interface IDatabase {
    @NotNull Collection<String> getTableNames();
    @NotNull UnassignedFilterMap<String> getQueryTypeFilterMap();
    @NotNull UnassignedFilterMap<String> getFilterMap(FilterWrapper<String> mainFilter);
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

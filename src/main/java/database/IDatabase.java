package database;

import lombok.NonNull;
import model.Role;
import model.User;
import util.AssignedFilterMap;
import util.UnassignedFilterMap;

import javax.swing.table.TableModel;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface IDatabase {
    @NonNull Collection<String> getTableNames();
    @NonNull UnassignedFilterMap<String> getQueryTypeFilterMap();
    @NonNull UnassignedFilterMap<String> getFilterMap(@NonNull String mainFilter);
    @NonNull Role authenticate(@NonNull User user) throws SQLException;
    void disconnect();
    void close();
    void register(@NonNull User user) throws Exception;
    void loadDataInfile(@NonNull String path, @NonNull String tableName);
    @NonNull Optional<TableModel> queryWithFilter(@NonNull String mainFilter,
                                                  @NonNull AssignedFilterMap<String> assignedFilterMap);
    void deleteFromWheres(@NonNull String tableName, @NonNull Collection<Map<String, Object>> wheres) throws SQLException;
    boolean isDisconnected();
    boolean hasPrivilegeOfImportingData(@NonNull User user);
    boolean hasPrivilegeOfRegisteringUser(@NonNull User user);
}

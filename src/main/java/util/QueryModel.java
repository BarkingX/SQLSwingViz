package util;

import lombok.NonNull;
import lombok.SneakyThrows;

import javax.sql.rowset.CachedRowSet;
import javax.swing.table.AbstractTableModel;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class QueryModel extends AbstractTableModel {
    private final CachedRowSet cachedRowSet;
    private final ResultSetMetaData metaData;

    public QueryModel(@NonNull CachedRowSet crs) throws SQLException {
        cachedRowSet = crs;
        metaData = crs.getMetaData();
    }

    @Override
    public int getRowCount() {
        return cachedRowSet.size();
    }

    @SneakyThrows
    @Override
    public int getColumnCount() {
        return metaData.getColumnCount();
    }

    @SneakyThrows
    @Override
    public Object getValueAt(int row, int column) {
        cachedRowSet.absolute(row + 1);
        return cachedRowSet.getObject(column + 1);
    }

    @SneakyThrows
    @Override
    public String getColumnName(int column) {
        return metaData.getColumnName(column + 1);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        try {
            var className = metaData.getColumnClassName(column + 1);
            return "java.sql.Time".equals(className) ? String.class : Class.forName(className);
        }
        catch (SQLException | ClassNotFoundException e) {
            return String.class;
        }
    }
}

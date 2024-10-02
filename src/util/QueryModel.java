package util;

import org.jetbrains.annotations.NotNull;

import javax.sql.rowset.CachedRowSet;
import javax.swing.table.AbstractTableModel;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class QueryModel extends AbstractTableModel {
    private final CachedRowSet cachedRowSet;
    private final ResultSetMetaData metaData;

    public QueryModel(@NotNull CachedRowSet crs) throws SQLException {
        cachedRowSet = crs;
        metaData = crs.getMetaData();
    }

    @Override
    public int getRowCount() {
        try {
            cachedRowSet.last();
            return cachedRowSet.getRow();
        }
        catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int getColumnCount() {
        try {
            return metaData.getColumnCount();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        try {
            cachedRowSet.absolute(row + 1);
            return cachedRowSet.getObject(column + 1);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        try {
            return metaData.getColumnName(column + 1);
        }
        catch (SQLException e) {
            return "column";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        try {
            var className = metaData.getColumnClassName(column + 1);
            if ("java.sql.Time".equals(className)) {
                return String.class;
            }
            else {
                return Class.forName(className);
            }
        }
        catch (SQLException | ClassNotFoundException e) {
            return String.class;
        }
    }
}

package util;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Metadata<T> extends List<T> {
    default int getColumnCount() {
        return this.size();
    }
    default @NotNull T getColumnLabel(int column) {
        return this.get(column - 1);
    }
    int getColumnDisplaySize(int column);
}

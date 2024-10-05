package util;


import lombok.NonNull;

import java.util.List;

public interface Metadata<T> extends List<T> {
    default int getColumnCount() {
        return this.size();
    }
    default @NonNull T getColumnLabel(int column) {
        return this.get(column - 1);
    }
    int getColumnDisplaySize(int column);
}

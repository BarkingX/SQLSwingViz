package util;


import lombok.NonNull;

import java.util.List;

//TODO: maybe use composition instead
public interface Metadata<T> extends List<T> {
    default int getColumnCount() {
        return this.size();
    }

    default @NonNull T getColumn(int column) {
        return this.get(column - 1);
    }

    int getDisplaySize(int column);
}

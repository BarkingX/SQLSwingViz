package util;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;


public class StrMetadata extends ArrayList<String> implements Metadata<String> {
    public StrMetadata(@NonNull String... columns) {
        addAll(List.of(columns));
    }

    @Override
    public int getDisplaySize(int column) {
        return 10;
    }
}
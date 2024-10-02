package util;

import java.util.ArrayList;
import java.util.List;

public class StrMetadata extends ArrayList<String> implements Metadata<String> {
    private static final int WIDTH = 10;

    public StrMetadata(String... columns) {
        addAll(List.of(columns));
    }

    @Override
    public int getColumnDisplaySize(int column) {
        return WIDTH;
    }
}
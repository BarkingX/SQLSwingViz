package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
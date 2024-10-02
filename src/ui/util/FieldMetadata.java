package ui.util;


import util.Metadata;

import javax.swing.*;
import java.util.LinkedList;

public class FieldMetadata extends LinkedList<JTextField> implements Metadata<JTextField> {
    private static final int WIDTH = 10;

    @Override
    public int getColumnDisplaySize(int column) {
        return WIDTH;
    }
}

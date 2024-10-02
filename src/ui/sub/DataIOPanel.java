package ui.sub;

import org.jetbrains.annotations.NotNull;
import ui.util.FieldMetadata;
import ui.util.GBC;
import util.Metadata;
import util.MetadataSupplier;

import javax.swing.*;
import java.awt.*;

public class DataIOPanel extends JPanel implements MetadataSupplier<String> {
    private final Metadata<String> metadata;
    private final FieldMetadata fields;
    private final GridBagConstraints gbc;

    public DataIOPanel(MetadataSupplier<String> metadataSupplier) {
        setLayout(new GridBagLayout());

        metadata = metadataSupplier.getMetadata();
        fields = new FieldMetadata();
        gbc = new GBC(0, 0, 1, 1);

        populate();
    }

    public void populate() {
        final int columnCount = metadata.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            var columnName = metadata.getColumnLabel(i);
            var columnWidth = metadata.getColumnDisplaySize(i);
            var label = new JLabel(columnName);
            var field = new JTextField(columnWidth);
            fields.add(field);
            addLabeledTextField(label, field);
        }
    }

    public void addLabeledTextField(JLabel label, JTextField textField) {
        gbc.gridy++;
        addJLabel(label);
        addTextField(textField);
    }

    private void addJLabel(JLabel label) {
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(label, gbc);
    }

    private void addTextField(JTextField textField) {
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(textField, gbc);
    }

    public void replaceWithPasswordField(int column) {
        var passwordField = new JPasswordField("", metadata.getColumnDisplaySize(column));
        replace(column, passwordField);
    }

    public void replace(int column, JTextField other) {
        remove(fields.remove(column - 1));
        int oldY = gbc.gridy;
        gbc.gridy = column;
        addTextField(other);
        gbc.gridy = oldY;
        fields.add(column - 1, other);
    }

    public void resetAllFields() {
        fields.forEach(field -> field.setText(""));
        fields.getFirst().requestFocus();
    }

    public @NotNull JTextField getTextField(int column) {
        var field = fields.getColumnLabel(column);
        return field instanceof JPasswordField ? (JPasswordField) field : field;
    }

    public @NotNull String getTextOf(int column) {
        var field = getTextField(column);
        return field instanceof JPasswordField ?
                String.valueOf(((JPasswordField) field).getPassword()) :
                field.getText();
    }

    @Override
    public Metadata<String> getMetadata() {
        return metadata;
    }
}

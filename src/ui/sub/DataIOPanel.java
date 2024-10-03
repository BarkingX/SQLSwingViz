package ui.sub;

import org.jetbrains.annotations.NotNull;
import ui.util.FieldMetadata;
import ui.util.GBC;
import util.Metadata;
import util.MetadataSupplier;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DataIOPanel extends JPanel implements MetadataSupplier<String> {
    private final Metadata<String> metadata;
    private final FieldMetadata fields = new FieldMetadata();
    private final GridBagConstraints gbc = new GBC(0, 0, 1, 1);

    public DataIOPanel(@NotNull MetadataSupplier<String> metadataSupplier) {
        metadata = metadataSupplier.getMetadata();
        setLayout(new GridBagLayout());
        populate();
    }

    public void populate() {
        Consumer<JLabel> addJLabel = label -> {
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.EAST;
            add(label, gbc);
        };
        BiConsumer<JLabel, JTextField> addLabeledTextField = (label, textField) -> {
            gbc.gridy++;
            addJLabel.accept(label);
            addTextField(textField);
        };

        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            var field = new JTextField(metadata.getColumnDisplaySize(i));
            fields.add(field);
            addLabeledTextField.accept(new JLabel(metadata.getColumnLabel(i)), field);
        }
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

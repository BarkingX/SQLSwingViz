package ui.sub;

import lombok.NonNull;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import ui.util.FieldMetadata;
import ui.util.GBC;
import util.Metadata;
import util.MetadataSupplier;

import javax.swing.*;
import java.awt.*;


import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Pattern;

public class DataIOPanel extends JXPanel implements MetadataSupplier<String> {
    private final Metadata<String> metadata;
    private final FieldMetadata fields = new FieldMetadata();
    private final GridBagConstraints gbc = new GBC(0, 0, 1, 1);

    public DataIOPanel(@NonNull MetadataSupplier<String> metadataSupplier) {
        metadata = metadataSupplier.getMetadata();
        setLayout(new GridBagLayout());
        populate();
    }

    public void populate() {
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            var textField = new JXTextField("Enter " + metadata.getColumnLabel(i));
            textField.setColumns(metadata.getColumnDisplaySize(i));
            setTextFieldValidation(textField);
            fields.add(textField);
            addFieldWithLabel(new JXLabel(metadata.getColumnLabel(i)), textField);
        }
    }

    private void addFieldWithLabel(JXLabel label, JXTextField textField) {
        addJLabel(label);
        addTextField(textField);
        gbc.gridy++;
    }

    private void addJLabel(JXLabel label) {
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
        var passwordField = new JPasswordField(metadata.getColumnDisplaySize(column));
        setPasswordFieldValidation(passwordField);

        remove(fields.remove(column - 1));
        gbc.gridy = column - 1;
        addTextField(passwordField);
        revalidate();
        repaint();
        fields.add(column - 1, passwordField);
    }

    private void setTextFieldValidation(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new AlphanumericFilter(16));
    }

    private void setPasswordFieldValidation(JPasswordField passwordField) {
        ((AbstractDocument) passwordField.getDocument()).setDocumentFilter(new AlphanumericFilter(16));
    }

    public void resetAllFields() {
        fields.forEach(field -> field.setText(""));
        fields.getFirst().requestFocus();
    }

    public @NonNull JTextField getTextField(int column) {
        return fields.getColumnLabel(column);
    }

    public @NonNull String getTextOf(int column) {
        var field = getTextField(column);
        return (field instanceof JPasswordField) ?
                String.valueOf(((JPasswordField) field).getPassword()) :
                field.getText();
    }

    @Override
    public Metadata<String> getMetadata() {
        return metadata;
    }

    static class AlphanumericFilter extends DocumentFilter {
        private final int maxLength;
        private final Pattern pattern = Pattern.compile("^[a-zA-Z0-9]*$");

        public AlphanumericFilter(int maxLength) {
            this.maxLength = maxLength;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null && isValid(fb.getDocument().getLength(), string)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null && isValid(fb.getDocument().getLength() - length, text)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValid(int currentLength, String newText) {
            return pattern.matcher(newText).matches() && (currentLength + newText.length() <= maxLength);
        }
    }
}


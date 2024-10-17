package ui.sub;

import lombok.NonNull;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import ui.util.GBC;
import util.Metadata;
import util.MetadataSupplier;

import java.awt.*;


import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DataIOPanel extends JXPanel implements MetadataSupplier<String> {
    private final Metadata<String> metadata;
    private final List<JTextField> fields = new ArrayList<>();
    private final GridBagConstraints gbc = new GBC(0, 0, 1, 1);

    public DataIOPanel(@NonNull MetadataSupplier<String> metadataSupplier) {
        metadata = metadataSupplier.getMetadata();
        setLayout(new GridBagLayout());
        addTextFields();
    }

    public void addTextFields() {
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            var textField = withValidation(new JXTextField("请输入" + metadata.getColumn(i)), 16);
            textField.setColumns(metadata.getDisplaySize(i));
            fields.add(textField);
            addFieldWithLabel(new JXLabel(metadata.getColumn(i)), textField);
        }
    }

    private @NonNull JTextField withValidation(@NonNull JTextField textField, int maxLength) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new AlphanumericFilter(maxLength));
        return textField;
    }

    private void addFieldWithLabel(JLabel label, JTextField textField) {
        addLeft(label);
        addRight(textField);
        gbc.gridy++;
    }

    private void addLeft(Component component) {
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(component, gbc);
    }

    private void addRight(Component component) {
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(component, gbc);
    }

    public void replaceWith(int index, Component component) {
        remove(fields.remove(index - 1));
        gbc.gridy = index - 1;
        addRight(component);
        revalidate();
        repaint();
    }

    public void replaceWithPasswordField(int index) {
        var passwordField = withValidation(new JPasswordField(metadata.getDisplaySize(index)), 16);
        replaceWith(index, passwordField);
        fields.add(index - 1, passwordField);
    }

    public void resetAllFields() {
        fields.forEach(field -> field.setText(""));
        fields.get(0).requestFocus();
    }

    public @NonNull String getTextOf(int index) {
        var field = fields.get(index - 1);
        return field instanceof JPasswordField
                ? String.valueOf(((JPasswordField) field).getPassword())
                : field.getText();
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


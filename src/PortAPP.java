
import ui.MainUI;
import database.Database;
import ui.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;


public class PortAPP {
    public static void main(String[] args) throws SQLException {
        try (final var db = new Database()) {
            EventQueue.invokeLater(() -> {
                var frame = new MainUI(db);
                Utils.centerWindow(frame);
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            });
        }
    }
}
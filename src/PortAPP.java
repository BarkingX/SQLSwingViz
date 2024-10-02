
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
        final var db = new Database();
        final var exit = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                db.close();
                System.exit(0);
            }
        };

        EventQueue.invokeLater(() -> {
            var frame = new MainUI(db, exit);
            Utils.centerWindow(frame);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        });
    }
}
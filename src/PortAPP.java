import ui.MainUI;
import database.Database;
import ui.util.Utils;

import java.awt.*;
import java.sql.SQLException;


public class PortAPP {
    public static void main(String[] args) throws SQLException {
        final var db = new Database();
        EventQueue.invokeLater(() -> {
            var frame = new MainUI(db);
            frame.addWindowListener(Utils.exitOnClosing(db::close));
            Utils.centerWindow(frame);
        });
    }
}
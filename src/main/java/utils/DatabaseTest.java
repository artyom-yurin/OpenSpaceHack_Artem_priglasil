package utils;

import org.json.JSONException;

import java.io.IOException;
import java.sql.SQLException;

public class DatabaseTest {
    public static void main(String[] args) throws JSONException, SQLException, IOException {
        DatabaseController controller = new DatabaseController();
        if (controller.establishConnection()) {
            System.out.println("Database connected");
        }
        controller.prepareDb();
        controller.initDb();
        controller.index();
    }
}

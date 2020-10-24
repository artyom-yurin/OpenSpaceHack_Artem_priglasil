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
        /*controller.prepareDb();
        controller.initDb();
        controller.index();*/
        /*Double[][] vects = controller.get_vectors();
        for (Double[] vect : vects) {
            for (Double arr : vect) {
                System.out.print(arr.toString() + ' ');
            }
            System.out.println();
        }*/
        System.out.println(controller.get_question(1));
        controller.closeConnection();
    }
}

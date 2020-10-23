package utils;

public class DatabaseTest {
    public static void main(String[] args) {
        DatabaseController controller = new DatabaseController();
        if (controller.establishConnection()) {
            System.out.println("Database connected");
        }
        controller.initDb();
    }
}

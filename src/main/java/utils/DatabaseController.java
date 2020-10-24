package utils;

import java.sql.*;

public class DatabaseController {
    //  Database credentials
    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/questions";
    static final String USER = "artem_priglasil";
    static final String PASS = "4r73m_pr1gl451l";
    private Connection connection;

    public boolean establishConnection() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            this.connection = DriverManager
                    .getConnection(DB_URL, USER, PASS);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Connection with PostgreSQL database established");
        return this.connection != null;
    }

    private void prepareDb() {
        String prep_query = "DROP TABLE IF EXISTS knowledge_base, vectors";
        try (PreparedStatement prSt = connection.prepareStatement(prep_query)) {
            prSt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database preparation failed");
        }
    }

    public boolean initDb() {
        prepareDb();
        String table_create = "CREATE TABLE IF NOT EXISTS knowledge_base(id SERIAL, " +
                "kb_id VARCHAR(256)," +
                "request TEXT NOT NULL, " +
                "request_type TEXT, " +
                "dbo_type TEXT, " +
                "question TEXT NOT NULL, " +
                "video_link TEXT, " +
                "faq_link TEXT, " +
                "use_link TEXT, " +
                "step1 TEXT, " +
                "step2 TEXT, " +
                "step3 TEXT, " +
                "step4 TEXT, " +
                "step5 TEXT, " +
                "step6 TEXT, " +
                "step7 TEXT, " +
                "step8 TEXT, " +
                "step9 TEXT, " +
                "step10 TEXT, " +
                "step11 TEXT, " +
                "step12 TEXT, " +
                "step13 TEXT, " +
                "step14 TEXT)";
        try (PreparedStatement prSt = connection.prepareStatement(table_create)) {
            prSt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database initialization failed");
        }
        String fill_db = "COPY knowledge_base(kb_id, request, request_type, dbo_type, question, video_link, faq_link, use_link, step1, step2, step3, step4, step5, step6, step7, step8, step9, step10, step11, step12, step13, step14) FROM '/app/KB.csv' DELIMITER ',' CSV HEADER";
        try (PreparedStatement prSt = connection.prepareStatement(fill_db)) {
            prSt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database filling failed");
        }
        return true;
    }
}

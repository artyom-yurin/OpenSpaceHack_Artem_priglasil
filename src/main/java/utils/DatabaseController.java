package utils;

import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import utils.RequestBody;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DatabaseController {
    //  Database credentials
    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/questions";
    static final String USER = "artem_priglasil";
    static final String PASS = "4r73m_pr1gl451l";
    private java.sql.Connection connection;
    Gson gson = new Gson();
    MediaType JSON = MediaType.get("application/json; charset=utf-8");

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

    public void prepareDb() {
        String prep_query = "DROP TABLE IF EXISTS knowledge_base, vectors";
        try (PreparedStatement prSt = connection.prepareStatement(prep_query)) {
            prSt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database preparation failed");
        }
    }

    public boolean initDb() {
        String table_create = "CREATE TABLE IF NOT EXISTS knowledge_base(id SERIAL PRIMARY KEY, " +
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
        String index_db = "CREATE TABLE IF NOT EXISTS vectors(" +
                "id INT PRIMARY KEY REFERENCES knowledge_base(id)," +
                "vector FLOAT[])";
        try (PreparedStatement prSt = connection.prepareStatement(index_db)) {
            prSt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database initialization failed" + e.toString());
        }
        String fill_db = "COPY knowledge_base(kb_id, request, request_type, dbo_type, question, video_link, faq_link, use_link, step1, step2, step3, step4, step5, step6, step7, step8, step9, step10, step11, step12, step13, step14) FROM '/app/KB.csv' DELIMITER ',' CSV HEADER";
        try (PreparedStatement prSt = connection.prepareStatement(fill_db)) {
            prSt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Database filling failed");
        }
        return true;
    }

    public boolean index() throws IOException, JSONException, SQLException {
        Statement get_questions = connection.createStatement();
        ResultSet questions = get_questions.executeQuery("SELECT id, question FROM knowledge_base");
        while (questions.next()) {
            // System.out.println(questions.getString("question"));
            ArrayList<String> texts = new ArrayList<>();
            texts.add(questions.getString("question"));
            RequestBody request = new utils.RequestBody("121", texts, false);
            String jsonStr = gson.toJson(request);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonStr, this.JSON);
            String response = RequestBody.make_post_request("http://10.241.1.243:8125/encode ", body);
            JSONObject responseObj = new JSONObject(response);
            // System.out.println(responseObj.toString());
            JSONArray vector = responseObj.getJSONArray("result").getJSONArray(0);

            double[] list = new double[vector.length()];
            for (int i = 0; i < vector.length(); i++) {
                list[i] = vector.getDouble(i);
            }



        }
        return true;
    }
}

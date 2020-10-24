package utils;

import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import models.RequestBody;
import models.DatabaseEntry;

import java.awt.dnd.DropTarget;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;


public class DatabaseController {
    //  Database credentials
    static final String DB_URL = "jdbc:postgresql://localhost:5432/questions";
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

    public void closeConnection() throws SQLException {
        this.connection.close();
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
            RequestBody request = new RequestBody("121", texts, false);
            String jsonStr = gson.toJson(request);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonStr, this.JSON);
            String response = RequestBody.make_post_request("http://indexer:8125/encode ", body);
            JSONObject responseObj = new JSONObject(response);
            // System.out.println(responseObj.toString());
            JSONArray vector = responseObj.getJSONArray("result").getJSONArray(0);

            Double[] list = new Double[vector.length()];
            for (int i = 0; i < vector.length(); i++) {
                list[i] = vector.getDouble(i);
            }
            String vectors_sql = "INSERT INTO vectors VALUES (?, ?)";
            PreparedStatement vector_obj = connection.prepareStatement(vectors_sql);
            vector_obj.setInt(1, questions.getInt("id"));
            java.sql.Array vect_arr = connection.createArrayOf("FLOAT", list);
            vector_obj.setArray(2, vect_arr);
            vector_obj.executeUpdate();
        }
        return true;
    }

    public Double[] get_vector(int _id) throws SQLException{
        String sql_get = "SELECT vector FROM vectors WHERE id = ?";
        PreparedStatement get_stmt = connection.prepareStatement(sql_get);
        get_stmt.setInt(1, _id);
        ResultSet vector = get_stmt.executeQuery();
        vector.next();
        Array vectors = vector.getArray("vector");
        return (Double[])vectors.getArray();
    }

    public Double[][] get_vectors() throws SQLException {
        String get_max_id = "SELECT id\n" +
                "FROM vectors \n" +
                "ORDER BY id DESC \n" +
                "LIMIT 1\n";
        Statement max_id_stmt = connection.createStatement();
        ResultSet max_id_result = max_id_stmt.executeQuery(get_max_id);
        max_id_result.next();
        int max_id = max_id_result.getInt("id");
        Double[][] vectors = new Double[max_id][max_id];
        for (int i = 1; i <= max_id; i++) {
            Double[] vector = get_vector(i);
            vectors[i - 1] = vector;
        }
        return vectors;
    }

    public DatabaseEntry get_question(int _id) throws SQLException {
        String separator = "\n";
        String sql_get = "SELECT id, kb_id, request, request_type, dbo_type, question, video_link, faq_link, use_link, step1, step2, step3, step4, step5, step6, step7, step8, step9, step10, step11, step12, step13, step14 FROM knowledge_base WHERE id = ?";
        PreparedStatement get_stmt = connection.prepareStatement(sql_get);
        get_stmt.setInt(1, _id);
        ResultSet vector = get_stmt.executeQuery();
        vector.next();
        String[] steps = new String[14];
        for (int i = 0; i < 14; i++) {
            steps[i] = vector.getString(i + 10);
        }
        String result = vector.getString("question") + separator;
        DatabaseEntry entry = new DatabaseEntry(vector.getInt("id"),
                vector.getString("kb_id"),
                vector.getString("request"),
                vector.getString("request_type"),
                vector.getString("dbo_type"),
                vector.getString("question"),
                vector.getString("video_link"),
                vector.getString("faq_link"),
                vector.getString("use_link"),
                steps);
        return entry;
    }
}

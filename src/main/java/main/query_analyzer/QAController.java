package main.query_analyzer;

import com.google.gson.Gson;
import okhttp3.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utils.CosineSimilarity;
import utils.DatabaseController;
import utils.MessageResponse;
import utils.RequestBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

@RestController
public class QAController {


    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    double[][] make_doc_matrix() throws FileNotFoundException, JSONException {
        ArrayList<double[]> docList = new ArrayList<>();
        File file = new File("path/to/file");
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()){
            String line = sc.nextLine();
            JSONObject object = new JSONObject(line);
            JSONArray innerArray = object.getJSONArray("result").getJSONArray(0);
            double[] tmp = new double[innerArray.length()];

            for (int i = 0; i < innerArray.length(); i++) {
                tmp[i] = innerArray.getDouble(i);
            }
            docList.add(tmp);

        }
        double[][] docMatrix = new double[docList.size()][768];
        return docList.toArray(docMatrix);
    }

    @GetMapping(value = "/api/chat/v1/bot", produces = "application/json")
    ResponseEntity<String> message(@RequestParam(value = "question", defaultValue = "") String question) throws IOException, JSONException, SQLException {
        if (question.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Question is empty");
        }

        RequestBody req_body = new RequestBody();
        req_body.setId("Some chat id");
        ArrayList<String> messages = new ArrayList<>();
        messages.add(question);
        req_body.setTexts(messages);
        req_body.set_tokenized(false);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(gson.toJson(req_body), JSON);
        JSONObject response = new JSONObject(RequestBody.make_post_request("http://localhost:8125/encode", body));
        JSONArray innerArray = response.getJSONArray("result").getJSONArray(0);

        DatabaseController controller = new DatabaseController();
        if (controller.establishConnection()) {
            System.out.println("Database connected");
        }
        Double[][] docMatrix = controller.get_vectors();
        double[] query_vector = new double[768];
        for (int i = 0; i < innerArray.length(); i++) {
            query_vector[i] = innerArray.getDouble(i);
        }
        double[] similarity = CosineSimilarity.cosine_similarity(docMatrix, query_vector);
        Map<Double, Integer> map = new HashMap<>();
        for (int i = 0; i < similarity.length; i++) {
            map.put(similarity[i], i+1);
        }
        List<Double> sortedMap = new ArrayList<>(map.keySet());
        sortedMap.sort(Collections.reverseOrder());

        int[] ids = new int[5];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = map.get(sortedMap.get(i));
        }
        controller.closeConnection();
        MessageResponse resp = new MessageResponse(controller.get_question(ids[0]));
        return ResponseEntity.ok(gson.toJson(resp));
    }


}

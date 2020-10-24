package main.query_analyzer;

import utils.CosineSimilarity;
import utils.JwtUtil;
import com.google.gson.Gson;
import models.ConversationData;
import models.MessageResponse;
import okhttp3.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import models.RequestBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

@RestController
public class QAController {

    private JwtUtil jwtUtil = new JwtUtil();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final Gson gson = new Gson();

    double[][] make_doc_matrix() throws FileNotFoundException, JSONException {
        ArrayList<double[]> docList = new ArrayList<>();
        File file = new File("path/to/file");
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
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
    ResponseEntity<String> message(@CookieValue(value = "OpenChat", defaultValue = "") String token, @RequestParam(value = "question", defaultValue = "") String question) throws IOException, JSONException {
        if (token.isEmpty()) {
            return ResponseEntity.status(401)
                    .body("I don't know you");
        }

        if (question.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Question is empty");
        }

        ConversationData converastion = jwtUtil.parseToken(token);
        if (converastion == null) {
            return ResponseEntity.status(401)
                    .body("I don't know you");
        }

        // TODO: get state and decide what to do

        RequestBody req_body = new RequestBody();
        req_body.setId(converastion.getChatId());
        ArrayList<String> messages = new ArrayList<>();
        messages.add(question);
        req_body.setTexts(messages);
        req_body.set_tokenized(false);

        okhttp3.RequestBody body = okhttp3.RequestBody.create(gson.toJson(req_body), JSON);
        JSONObject response = new JSONObject(RequestBody.make_post_request("http://10.241.1.243:8125/encode", body));
        JSONArray innerArray = response.getJSONArray("result").getJSONArray(0);

        double[][] docMatrix = {};//make_doc_matrix();
        double[] query_vector = new double[768];
        for (int i = 0; i < innerArray.length(); i++) {
            query_vector[i] = innerArray.getDouble(i);
        }
        double[] similarity = CosineSimilarity.cosine_similarity(docMatrix, query_vector);
        System.out.println(similarity.length);
        System.out.println(Arrays.toString(query_vector));

        MessageResponse resp = new MessageResponse("answer");
        return ResponseEntity.ok(gson.toJson(resp));
    }


}

package main.query_analyzer;

import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import utils.CosineSimilarity;
import utils.Request;
import utils.Reponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

@RestController
public class QAController {


    MediaType JSON;
    Gson gson;

    QAController() {
        this.JSON = MediaType.get("application/json; charset=utf-8");
        this.gson = new Gson();
    }

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

    @PostMapping("/query")
    ResponseEntity<String> query(@RequestBody Request query) throws IOException, JSONException {
        okhttp3.RequestBody body = okhttp3.RequestBody.create(query.toString(), this.JSON);
        JSONObject response = new JSONObject(Request.make_post_request("http://localhost:8125/encode", body));
        JSONArray innerArray = response.getJSONArray("result").getJSONArray(0);

        double[][] docMatrix = make_doc_matrix();
        double[] query_vector = new double[768];
        for (int i = 0; i < innerArray.length(); i++) {
            query_vector[i] = innerArray.getDouble(i);
        }
        double[] similarity = CosineSimilarity.cosine_similarity(docMatrix, query_vector);
        System.out.println(similarity.length);
        return new ResponseEntity<>(Arrays.toString(similarity), HttpStatus.OK);
    }


}

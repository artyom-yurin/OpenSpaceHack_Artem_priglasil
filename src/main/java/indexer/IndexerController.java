package indexer;

import com.google.gson.Gson;
import jdk.nashorn.internal.parser.JSONParser;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

@RestController
public class IndexerController {
    MediaType JSON = null;
    Gson gson;

    IndexerController() {
        this.JSON = MediaType.get("application/json; charset=utf-8");
        this.gson = new Gson();
    }

    String make_post_request(String url, okhttp3.RequestBody body) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get response body
            return response.body().string();
        }
    }

    String make_body(long id, ArrayList<String> texts, boolean is_tokenized){
        return String.format("{\"id\":%d, \"texts\":%s, \"is_tokenized\": %b", id, texts.toString(), is_tokenized);
    }

    @PostMapping("/query_vector")
    ResponseEntity<String> query_vector(@RequestBody IndexerRequest query) throws IOException {
        System.out.println(query.toString());
        okhttp3.RequestBody body = okhttp3.RequestBody.create(query.toString(), this.JSON);
        String response = make_post_request("http://localhost:8125/encode", body);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/index")
    ResponseEntity<String> index() throws IOException, JSONException {
        File file = new File("src/main/resources/KnowledgeBase/wiki");
        Scanner sc = new Scanner(file);
        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/KnowledgeBase/index"));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            JSONObject requestObj = new JSONObject(line);
            ArrayList<String> texts = new ArrayList<>();
            texts.add(requestObj.get("text").toString());
            IndexerRequest request = new IndexerRequest(requestObj.get("id").toString(), texts, false);
            String jsonStr = gson.toJson(request);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonStr, this.JSON);
            String response = make_post_request("http://localhost:8125/encode", body);
            JSONObject responseObj = new JSONObject(response);
            responseObj.accumulate("url", requestObj.get("url"));
            writer.write(responseObj.toString());
        }
        sc.close();
        writer.close();
        return new ResponseEntity<>("Indexing done", HttpStatus.OK);
    }
}

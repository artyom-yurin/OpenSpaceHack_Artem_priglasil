package main.indexer;

import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.Request;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

@RestController
public class IndexerController {
    MediaType JSON;
    Gson gson;

    IndexerController() {
        this.JSON = MediaType.get("application/json; charset=utf-8");
        this.gson = new Gson();
    }



    @GetMapping("/index")
    ResponseEntity<String> index() throws IOException, JSONException {
        File file = new File("path/to/file");
        Scanner sc = new Scanner(file);
        PrintWriter writer = new PrintWriter(new FileWriter("path/to/file"));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            JSONObject requestObj = new JSONObject(line);
            ArrayList<String> texts = new ArrayList<>();
            texts.add(requestObj.get("text").toString());
            Request request = new Request(requestObj.get("id").toString(), texts, false);
            String jsonStr = gson.toJson(request);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonStr, this.JSON);
            String response = Request.make_post_request("http://localhost:8125/encode", body);
            JSONObject responseObj = new JSONObject(response);
            responseObj.accumulate("url", requestObj.get("url"));
            writer.println(responseObj.toString());
        }
        sc.close();
        writer.close();
        return new ResponseEntity<>("Indexing done", HttpStatus.OK);
    }
}

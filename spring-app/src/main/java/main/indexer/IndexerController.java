package main.indexer;

import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.DatabaseController;

import java.io.*;
import java.sql.SQLException;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class IndexerController {
    MediaType JSON;
    Gson gson;

    IndexerController() {
        this.JSON = MediaType.get("application/json; charset=utf-8");
        this.gson = new Gson();
    }



    @GetMapping("/index")
    ResponseEntity<String> index() throws IOException, JSONException, SQLException {
//        File file = new File("path/to/file");
//        Scanner sc = new Scanner(file);
//        PrintWriter writer = new PrintWriter(new FileWriter("path/to/file"));
//        while (sc.hasNextLine()) {
//            String line = sc.nextLine();
//            JSONObject requestObj = new JSONObject(line);
//            ArrayList<String> texts = new ArrayList<>();
//            texts.add(requestObj.get("text").toString());
//            RequestBody request = new RequestBody(requestObj.get("id").toString(), texts, false);
//            String jsonStr = gson.toJson(request);
//            okhttp3.RequestBody body = okhttp3.RequestBody.create(jsonStr, this.JSON);
//            String response = RequestBody.make_post_request("http://localhost:8125/encode", body);
//            JSONObject responseObj = new JSONObject(response);
//            responseObj.accumulate("url", requestObj.get("url"));
//            writer.println(responseObj.toString());
//        }
//        sc.close();
//        writer.close();
        DatabaseController controller = new DatabaseController();
        if (controller.establishConnection()) {
            System.out.println("Database connected");
        }
        controller.prepareDb();
        controller.initDb();
        controller.index();
        controller.closeConnection();
        return new ResponseEntity<>("Indexing done", HttpStatus.OK);
    }
}

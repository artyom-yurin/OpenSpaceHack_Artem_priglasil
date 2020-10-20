package indexer;

import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class IndexerController {
    MediaType JSON = null;
    IndexerController(){
        this.JSON = MediaType.get("application/json; charset=utf-8");
    }

    @PostMapping("/get/query_vector")
    ResponseEntity<String> query_vector(@RequestBody IndexerRequest query) throws IOException {
        final OkHttpClient httpClient = new OkHttpClient();
//        okhttp3.RequestBody formBody = new FormBody.Builder()
//                .add("id", query.getId())
//                .add("texts", String.valueOf(query.getTexts()))
//                .add("is_tokenized", String.valueOf(query.is_tokenized()))
//                .build();
        System.out.println(query.toString()+"afafa");
        okhttp3.RequestBody body = okhttp3.RequestBody.create(query.toString(), this.JSON);
        Request request = new Request.Builder()
                .url("http://localhost:8125/encode")
                .post(body)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get response body
            return new ResponseEntity<String>(response.body().string(), HttpStatus.OK);
        }

    }
}

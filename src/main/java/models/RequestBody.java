package models;

import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;

public class RequestBody {
    private String id;
    private ArrayList<String> texts;
    private boolean is_tokenized;

    public RequestBody(){}

    public RequestBody(String id, ArrayList<String> texts, boolean is_tokenized){
        this.id = id;
        this.texts = texts;
        this.is_tokenized = is_tokenized;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getTexts() {
        return texts;
    }

    public void setTexts(ArrayList<String> texts) {
        this.texts = texts;
    }

    public boolean is_tokenized() {
        return is_tokenized;
    }

    public void set_tokenized(boolean is_tokenized) {
        this.is_tokenized = is_tokenized;
    }

    public static String make_post_request(String url, okhttp3.RequestBody body) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Get response body
            return response.body().string();
        }
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":"+ id +
                ", \"texts\":" + texts.toString()
                                      .replace("[","[\"")
                                      .replace("]","\"]")
                                      .replaceAll("\n","")
                                      .replaceAll("[.]"," ||| ")
                + ", \"is_tokenized\":" + is_tokenized + '}';
    }
}


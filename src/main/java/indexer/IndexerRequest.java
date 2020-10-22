package indexer;

import java.util.ArrayList;

public class IndexerRequest {
    private String id;
    private ArrayList<String> texts;
    private boolean tokenized;

    IndexerRequest(){}

    IndexerRequest(String id, ArrayList<String> texts, boolean is_tokenized){
        this.id = id;
        this.texts = texts;
        this.tokenized = is_tokenized;
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
        return tokenized;
    }

    public void set_tokenized(boolean is_tokenized) {
        this.tokenized = is_tokenized;
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\":"+ id +
                ", \"texts\":" + texts.toString().replace("[","[\"").replace("]","\"]").replaceAll("\n"," ") +
                ", \"is_tokenized\":" + tokenized +
                '}';
    }
}


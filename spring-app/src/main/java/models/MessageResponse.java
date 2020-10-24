package models;

import org.springframework.lang.NonNull;

public class MessageResponse {

    @NonNull
    private String answer;

    public MessageResponse(@NonNull String answer) {
        this.answer = answer;
    }

    @NonNull
    public String getAnswer() {
        return answer;
    }

    public void setAnswer(@NonNull String answer) {
        this.answer = answer;
    }
}

package models;

public class ConversationData {

    private String chatId;
    private ConversationState state;

    public ConversationData(String chatId) {
        this.chatId = chatId;
        this.state = ConversationState.Init;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }
}

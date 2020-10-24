package models;

public class Context {

    private ConversationState state;

    public Context(){
        state = ConversationState.Init;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }
}
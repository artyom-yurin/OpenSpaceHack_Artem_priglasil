package models;

import java.util.*;

public class Context {

    private String originalQuestion;
    private ConversationState state;
    private Map<Integer, ContextCandidate> candidates;
    private Set<String> categories; // if 1, then it is determined
    private Set<String> types; // if 1, then it is determined
    private int counter;


    public Context() {
        state = ConversationState.Init;
        candidates = new LinkedHashMap<>();
        categories = new HashSet<>();
        types = new HashSet<>();
        counter = 0;
    }

    public String getOriginalQuestion() {
        return originalQuestion;
    }

    public void setOriginalQuestion(String originalQuestion) {
        this.originalQuestion = originalQuestion;
    }

    public void resetCounter() {
        counter = 0;
    }

    public void newRequest() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public Map<Integer, ContextCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(Map<Integer, ContextCandidate> candidates) {
        this.candidates = candidates;
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }
}
package models;

public class ContextCandidate {
    private Double score;
    private DatabaseEntry databaseEntry;

    public ContextCandidate(Double score, DatabaseEntry databaseEntry) {
        this.score = score;
        this.databaseEntry = databaseEntry;
    }

    public Double getScore() {
        return score;
    }

    public DatabaseEntry getDatabaseEntry() {
        return databaseEntry;
    }
}

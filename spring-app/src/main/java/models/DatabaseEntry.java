package models;

public class DatabaseEntry {
    public int db_id;
    public String kb_id;
    public String request;
    public String request_type;
    public String dbo_type;
    public String question;
    public String video_link;
    public String faq_link;
    public String use_link;
    public String[] steps;

    public DatabaseEntry(int db_id,
                         String kb_id,
                         String request,
                         String request_type,
                         String dbo_type,
                         String question,
                         String video_link,
                         String faq_link,
                         String use_link,
                         String[] steps) {
        this.db_id = db_id;
        this.kb_id = kb_id;
        this.request = request;
        this.request_type = request_type;
        this.dbo_type = dbo_type;
        this.question = question;
        this.video_link = video_link;
        this.faq_link = faq_link;
        this.use_link = use_link;
        this.steps = steps;
    }

    public String getAnswer() {
        String separator = "\n";
        String res = "";
        for (int i = 0; i < steps.length; i++) {
            if (steps[i] != null) {
                res += (i + 1) + ". " + steps[i] + separator;
            }
        }
        return res;
    }
}

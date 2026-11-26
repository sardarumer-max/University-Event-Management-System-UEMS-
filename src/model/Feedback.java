package model;

/** Rating and comment given by a student for an attended event (FR-13). */
public class Feedback implements Validatable {

    private int feedbackId;
    private int rating;
    private String comment;
    private String authorName;        // association "written by"
    private boolean saved;
    private String validationMessage = "";

    public Feedback(int feedbackId, int rating, String comment, String authorName) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.comment = comment;
        this.authorName = authorName;
        this.saved = false;
    }

    @Override
    public boolean validate() {
        if (rating < 1 || rating > 5) {
            validationMessage = "rating must be between 1 and 5";
            return false;
        }
        if (comment == null || comment.trim().length() == 0) {
            validationMessage = "comment is empty";
            return false;
        }
        validationMessage = "OK";
        return true;
    }

    @Override
    public String getValidationMessage() {
        return validationMessage;
    }

    public void save() {
        saved = true;
    }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getAuthorName() { return authorName; }

    public boolean isSaved() { return saved; }
}

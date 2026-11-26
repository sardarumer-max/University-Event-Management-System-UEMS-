package model;

/**
 * Abstract notification (FR-11). Children: EmailNotification, SMSNotification.
 * The sender only knows "a Notification" and calls send() - runtime polymorphism.
 */
public abstract class Notification {

    private int notificationId;
    private String message;
    private String status;
    private User recipient;          // association "sent to" (Notification -> User)

    public Notification(int notificationId, User recipient, String message) {
        this.notificationId = notificationId;
        this.recipient = recipient;
        this.message = message;
        this.status = "CREATED";
    }

    /** Each channel delivers the message in its own way. */
    public abstract boolean send();

    /** Concrete method shared by all channels: fills the {name} placeholder. */
    public String formatMessage(String template) {
        return "Dear " + recipient.getName() + ", " + template;
    }

    public void markDelivered() {
        status = "DELIVERED";
    }

    public int getNotificationId() { return notificationId; }
    public void setNotificationId(int notificationId) { this.notificationId = notificationId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
}

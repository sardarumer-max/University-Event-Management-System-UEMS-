package model;

/** SMS channel: short text, cut to maxLength characters. */
public class SMSNotification extends Notification {

    private String phoneNumber;
    private String senderId;
    private int maxLength;

    public SMSNotification(int notificationId, User recipient, String phoneNumber, String message) {
        super(notificationId, recipient, message);
        this.phoneNumber = phoneNumber;
        this.senderId = "UEMS";
        this.maxLength = 100;
    }

    @Override
    public boolean send() {
        System.out.println("  [SMS from " + senderId + "] To: " + phoneNumber + " -> " + truncate());
        markDelivered();
        return true;
    }

    /** Cuts the formatted text so it fits in one SMS. */
    public String truncate() {
        String text = super.formatMessage(getMessage());        // super in a method
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getSenderId() { return senderId; }
    public void setSender(String senderId) { this.senderId = senderId; }

    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }
}

package model;

/** E-mail channel: builds an e-mail with subject and optional ticket attachment. */
public class EmailNotification extends Notification {

    private String emailAddress;
    private String subject;
    private String smtpHost;
    private String attachmentPath;

    public EmailNotification(int notificationId, User recipient, String subject, String message) {
        super(notificationId, recipient, message);
        this.emailAddress = recipient.getEmail();
        this.subject = subject;
        this.smtpHost = "smtp.uems.edu.pk";
        this.attachmentPath = "";
    }

    @Override
    public boolean send() {
        String body = super.formatMessage(getMessage());        // super in a method
        System.out.println("  [EMAIL via " + smtpHost + "] To: " + emailAddress);
        System.out.println("     Subject: " + subject);
        System.out.println("     " + body);
        if (attachmentPath.length() > 0) {
            System.out.println("     Attachment: " + attachmentPath);
        }
        markDelivered();
        return true;
    }

    public void attachTicket(Ticket t) {
        attachmentPath = "e-ticket-" + t.getTicketId() + " (QR " + t.getQrCode() + ")";
    }

    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSmtpHost() { return smtpHost; }
    public void setSmtpHost(String smtpHost) { this.smtpHost = smtpHost; }

    public String getAttachmentPath() { return attachmentPath; }
}

package model;

/** QR e-ticket. Part of a Registration (composition) - created only by Registration. */
public class Ticket {

    private int ticketId;
    private String qrCode;
    private String issuedAt;
    private boolean used;

    public Ticket(int ticketId, int regId, String issuedAt) {
        this.ticketId = ticketId;
        this.qrCode = generateQR(regId);
        this.issuedAt = issuedAt;
        this.used = false;
    }

    /** FR-06: unique code built from the registration id. */
    public String generateQR(int regId) {
        int check = (regId * 7919 + 13) % 1000;          // simple check digits
        return "QR" + regId + "-" + check;
    }

    /** FR-10: valid only if the code matches and the ticket was not used before. */
    public boolean validate(String qr) {
        return qrCode.equals(qr) && !used;
    }

    public void markUsed() {
        used = true;
    }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public String getQrCode() { return qrCode; }

    public String getIssuedAt() { return issuedAt; }
    public void setIssuedAt(String issuedAt) { this.issuedAt = issuedAt; }

    public boolean isUsed() { return used; }
}

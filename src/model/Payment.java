package model;

/** Fee payment for one registration (FR-08, FR-09). Status: PENDING, PAID, FAILED, REFUNDED. */
public class Payment {

    private int paymentId;
    private double amount;
    private String status;
    private String transactionRef;
    private int attempts;
    private Registration registration;   // one-way association "pays for"
    private PaymentGateway gateway;      // association "processed via"

    public Payment(int paymentId, Registration registration, double amount, PaymentGateway gateway) {
        this.paymentId = paymentId;
        this.registration = registration;
        this.amount = amount;
        this.gateway = gateway;
        this.status = "PENDING";
        this.transactionRef = "";
        this.attempts = 0;
    }

    /** One payment attempt through the gateway. walletBalance simulates the student's account. */
    public boolean process(double walletBalance) {
        attempts++;
        String ref = gateway.charge(amount, "REG-" + registration.getRegId(), walletBalance);
        if (ref != null) {
            transactionRef = ref;
            status = "PAID";
            return true;
        }
        status = "FAILED";
        return false;
    }

    /** FR-09: refund a paid fee. */
    public boolean refund() {
        if (status.equals("PAID") && gateway.refund(transactionRef)) {
            status = "REFUNDED";
            return true;
        }
        return false;
    }

    public String generateReceipt() {
        return "RECEIPT #" + paymentId + " | Reg " + registration.getRegId() + " | Rs " + amount
             + " | " + status + " | Txn " + transactionRef;
    }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public String getTransactionRef() { return transactionRef; }
    public int getAttempts() { return attempts; }
    public Registration getRegistration() { return registration; }
}

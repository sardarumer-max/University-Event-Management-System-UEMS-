package model;

/**
 * <<boundary>> class standing in for the external payment gateway.
 * No real money is moved: the console demo passes a simulated wallet balance.
 */
public class PaymentGateway {

    private String merchantId;
    private String apiUrl;
    private int timeoutSec;
    private int transactionCounter;

    public PaymentGateway(String merchantId, String apiUrl, int timeoutSec) {
        this.merchantId = merchantId;
        this.apiUrl = apiUrl;
        this.timeoutSec = timeoutSec;
        this.transactionCounter = 1000;
    }

    /** Returns a transaction reference, or null if the charge is declined. */
    public String charge(double amount, String ref, double walletBalance) {
        if (amount > 0 && walletBalance >= amount) {
            transactionCounter++;
            return "TXN" + transactionCounter;
        }
        return null;
    }

    public boolean refund(String txnRef) {
        return txnRef != null && txnRef.startsWith("TXN");
    }

    public String getStatus(String txnRef) {
        if (txnRef != null && txnRef.startsWith("TXN")) {
            return "SETTLED";
        }
        return "UNKNOWN";
    }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public int getTimeoutSec() { return timeoutSec; }
    public void setTimeoutSec(int timeoutSec) { this.timeoutSec = timeoutSec; }
}

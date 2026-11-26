package model;

/**
 * <<controller>> - holds the registration rules that involve several classes
 * (Event, Registration, Payment, Notification). Implements UC-03, UC-04, UC-05, UC-06.
 */
public class RegistrationManager {

    private int maxAttempts;
    private int cancelWindowHours;
    private int activeRequests;
    private Registration[] registrations = new Registration[100];
    private int regCount = 0;
    private Payment[] payments = new Payment[100];
    private int payCount = 0;
    private PaymentGateway gateway;
    private int nextNotificationId = 1;

    public RegistrationManager(PaymentGateway gateway, int maxAttempts, int cancelWindowHours) {
        this.gateway = gateway;
        this.maxAttempts = maxAttempts;
        this.cancelWindowHours = cancelWindowHours;
        this.activeRequests = 0;
    }

    // =============== UC-03 Register for Event (stepwise refinement of Assignment 3) ===============

    public Registration register(Student s, Event e, double walletBalance) {
        activeRequests++;
        // 1. Check eligibility
        if (e == null || !(e.getStatus().equals("OPEN") || e.getStatus().equals("FULL"))) {
            System.out.println("  Registration closed for this event.");
            return null;
        }
        if (isDuplicate(s, e)) {
            System.out.println("  You are already registered for this event.");
            return null;
        }
        // 2. Reserve a place
        Registration r = new Registration(100 + regCount + 1, s, e);
        registrations[regCount] = r;
        regCount++;
        if (!e.hasSeats()) {                                           // alternative flow 4a
            int pos = r.joinWaitlist(countWaitlisted(e) + 1);
            notifyUser(new SMSNotification(nextNotificationId++, s, s.getPhone(),
                    "the event '" + e.getTitle() + "' is full. You are number " + pos + " on the waitlist."));
            return r;
        }
        // 3. Collect the fee
        if (e.isPaid()) {
            Payment p = new Payment(500 + payCount + 1, r, e.getFee(), gateway);
            payments[payCount] = p;
            payCount++;
            if (!p.process(walletBalance)) {                           // exception flow 7a
                System.out.println("  Payment declined (attempt " + p.getAttempts() + " of " + maxAttempts
                        + "). Registration " + r.getRegId() + " is PENDING - use 'Retry payment'.");
                return r;
            }
            System.out.println("  " + p.generateReceipt());
        }
        // 4. Finish
        finish(r);
        return r;
    }

    /** UC-04 Make Payment: another attempt for a PENDING registration (max 3 attempts). */
    public boolean retryPayment(int regId, double walletBalance) {
        Registration r = findRegistration(regId);
        Payment p = findPayment(regId);
        if (r == null || p == null || !r.getStatus().equals("PENDING")) {
            System.out.println("  No pending payment for registration " + regId + ".");
            return false;
        }
        if (p.process(walletBalance)) {
            System.out.println("  " + p.generateReceipt());
            if (r.getEvent().hasSeats()) {
                finish(r);
            } else {
                p.refund();
                r.joinWaitlist(countWaitlisted(r.getEvent()) + 1);
                System.out.println("  Event filled up meanwhile: fee refunded, you are on the waitlist.");
            }
            return true;
        }
        if (p.getAttempts() >= maxAttempts) {
            r.cancel();
            System.out.println("  Payment failed " + maxAttempts + " times. Registration " + regId + " cancelled.");
        } else {
            System.out.println("  Payment declined (attempt " + p.getAttempts() + " of " + maxAttempts + ").");
        }
        return false;
    }

    /** Step 4: confirm, take the seat, create the ticket and e-mail it. */
    private void finish(Registration r) {
        Event e = r.getEvent();
        r.confirm();
        e.decrementSeats();
        Ticket t = r.generateTicket();
        EmailNotification email = new EmailNotification(nextNotificationId++, r.getStudent(),
                "Registration confirmed: " + e.getTitle(),
                "your seat for '" + e.getTitle() + "' on " + e.getStartTime() + " is confirmed (Reg "
                        + r.getRegId() + ").");
        email.attachTicket(t);
        notifyUser(email);
    }

    // =============== UC-06 Cancel Registration ===============

    public boolean cancel(int regId) {
        Registration r = findRegistration(regId);
        if (r == null || r.getStatus().equals("CANCELLED")) {
            System.out.println("  Registration " + regId + " not found or already cancelled.");
            return false;
        }
        Event e = r.getEvent();
        if (e.getHoursToStart() < cancelWindowHours) {               // FR-09: 24-hour rule
            System.out.println("  Too late: cancellation is allowed only up to " + cancelWindowHours
                    + " hours before the event.");
            return false;
        }
        boolean wasConfirmed = r.getStatus().equals("CONFIRMED");
        boolean wasWaitlisted = r.getStatus().equals("WAITLISTED");
        int oldPosition = r.getWaitlistPosition();
        r.cancel();

        Payment p = findPayment(regId);
        if (p != null && p.refund()) {
            System.out.println("  Refund issued: " + p.generateReceipt());
        }
        notifyUser(new SMSNotification(nextNotificationId++, r.getStudent(), r.getStudent().getPhone(),
                "your registration " + regId + " for '" + e.getTitle() + "' is cancelled."));

        if (wasConfirmed) {
            e.releaseSeat();
            promoteFromWaitlist(e);
        } else if (wasWaitlisted) {
            shiftWaitlist(e, oldPosition);
        }
        return true;
    }

    /** FR-07: the first student on the waitlist gets the free seat. */
    public Registration promoteFromWaitlist(Event e) {
        Registration first = null;
        for (int i = 0; i < regCount; i++) {
            if (registrations[i].getEvent() == e && registrations[i].getStatus().equals("WAITLISTED")
                    && registrations[i].getWaitlistPosition() == 1) {
                first = registrations[i];
            }
        }
        if (first == null) {
            return null;
        }
        shiftWaitlist(e, 1);
        System.out.println("  Waitlist: " + first.getStudent().getName() + " (Reg " + first.getRegId()
                + ") is promoted.");
        if (e.isPaid()) {
            first.setStatus("PENDING");
            first.setWaitlistPosition(0);
            payments[payCount] = new Payment(500 + payCount + 1, first, e.getFee(), gateway);
            payCount++;
            System.out.println("  Fee of Rs " + e.getFee() + " is due - use 'Retry payment' to pay.");
        } else {
            finish(first);
        }
        return first;
    }

    /** Moves everybody behind 'position' one place forward. */
    private void shiftWaitlist(Event e, int position) {
        for (int i = 0; i < regCount; i++) {
            Registration r = registrations[i];
            if (r.getEvent() == e && r.getStatus().equals("WAITLISTED") && r.getWaitlistPosition() > position) {
                r.setWaitlistPosition(r.getWaitlistPosition() - 1);
            }
        }
    }

    private boolean isDuplicate(Student s, Event e) {
        for (int i = 0; i < regCount; i++) {
            if (registrations[i].getStudent() == s && registrations[i].getEvent() == e
                    && !registrations[i].getStatus().equals("CANCELLED")) {
                return true;
            }
        }
        return false;
    }

    private int countWaitlisted(Event e) {
        int n = 0;
        for (int i = 0; i < regCount; i++) {
            if (registrations[i].getEvent() == e && registrations[i].getStatus().equals("WAITLISTED")) {
                n++;
            }
        }
        return n;
    }

    /** Parameter of the parent type: any Notification child can be passed (runtime polymorphism). */
    private void notifyUser(Notification n) {
        n.send();
    }

    // =============== search helpers (method overloading) ===============

    public Event findEvent(Event[] events, int count, int eventId) {
        for (int i = 0; i < count; i++) {
            if (events[i].getEventId() == eventId) {
                return events[i];
            }
        }
        return null;
    }

    public Event findEvent(Event[] events, int count, String title) {
        for (int i = 0; i < count; i++) {
            if (events[i].getTitle().equalsIgnoreCase(title)) {
                return events[i];
            }
        }
        return null;
    }

    public Registration findRegistration(int regId) {
        for (int i = 0; i < regCount; i++) {
            if (registrations[i].getRegId() == regId) {
                return registrations[i];
            }
        }
        return null;
    }

    private Payment findPayment(int regId) {
        Payment last = null;
        for (int i = 0; i < payCount; i++) {
            if (payments[i].getRegistration().getRegId() == regId) {
                last = payments[i];
            }
        }
        return last;
    }

    /** Prints all registrations of one student. */
    public int printRegistrations(Student s) {
        int n = 0;
        for (int i = 0; i < regCount; i++) {
            Registration r = registrations[i];
            if (r.getStudent() == s) {
                String line = "  Reg " + r.getRegId() + " | " + r.getEvent().getTitle() + " | " + r.getStatus();
                if (r.getStatus().equals("WAITLISTED")) {
                    line += " (position " + r.getWaitlistPosition() + ")";
                } else if (r.getTicket() != null) {
                    line += " | QR " + r.getTicket().getQrCode();
                }
                System.out.println(line);
                n++;
            }
        }
        return n;
    }

    // =============== getters / setters ===============

    public Registration[] getRegistrations() { return registrations; }
    public int getRegCount() { return regCount; }

    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

    public int getCancelWindowHours() { return cancelWindowHours; }
    public void setCancelWindowHours(int cancelWindowHours) { this.cancelWindowHours = cancelWindowHours; }

    public int getActiveRequests() { return activeRequests; }
}

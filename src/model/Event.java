package model;

/**
 * Central entity of UEMS (FR-02..FR-05).
 * Status values follow the Assignment 2 state diagram:
 * DRAFT -> PENDING -> OPEN <-> FULL, or REJECTED / CANCELLED.
 */
public class Event implements Validatable {

    private int eventId;
    private String title;
    private String startTime;
    private int hoursToStart;
    private int capacity;
    private int seatsTaken;
    private double fee;
    private String status;
    private String remarks;
    private String organizerName;
    private Venue venue;                                // association "held at"
    private Feedback[] feedbacks = new Feedback[50];    // aggregation "collects"
    private int feedbackCount = 0;
    private String validationMessage = "";

    // Constructor 1: paid event
    public Event(int eventId, String title, String startTime, int hoursToStart,
                 int capacity, double fee, Venue venue) {
        this.eventId = eventId;
        this.title = title;
        this.startTime = startTime;
        this.hoursToStart = hoursToStart;
        this.capacity = capacity;
        this.fee = fee;
        this.venue = venue;
        this.seatsTaken = 0;
        this.status = "DRAFT";
        this.remarks = "";
    }

    // Constructor 2 (overloaded): free event, fee = 0
    public Event(int eventId, String title, String startTime, int hoursToStart, int capacity, Venue venue) {
        this(eventId, title, startTime, hoursToStart, capacity, 0.0, venue);
    }

    // ---------- Validatable ----------

    @Override
    public boolean validate() {
        if (title == null || title.trim().length() == 0) {
            validationMessage = "title is empty";
        } else if (capacity <= 0 || capacity > venue.getCapacity()) {
            validationMessage = "capacity must be 1.." + venue.getCapacity() + " for " + venue.getName();
        } else if (fee < 0) {
            validationMessage = "fee cannot be negative";
        } else if (hoursToStart <= 0) {
            validationMessage = "event must be in the future";
        } else {
            validationMessage = "OK";
            return true;
        }
        return false;
    }

    @Override
    public String getValidationMessage() {
        return validationMessage;
    }

    // ---------- operations from the class diagram ----------

    public boolean submit() {
        if (validate()) {
            status = "PENDING";
            return true;
        }
        return false;
    }

    public void publish() {
        status = "OPEN";
    }

    public boolean hasSeats() {
        return seatsTaken < capacity;
    }

    public boolean isPaid() {
        return fee > 0;
    }

    public void decrementSeats() {
        seatsTaken++;
        if (seatsTaken == capacity) {
            status = "FULL";
        }
    }

    public void releaseSeat() {
        if (seatsTaken > 0) {
            seatsTaken--;
        }
        if (status.equals("FULL") && seatsTaken < capacity) {
            status = "OPEN";
        }
    }

    public void cancel(String reason) {
        status = "CANCELLED";
        remarks = reason;
        venue.release(eventId);
    }

    public void addFeedback(Feedback f) {
        if (feedbackCount < feedbacks.length) {
            feedbacks[feedbackCount] = f;
            feedbackCount++;
        }
    }

    /** Average star rating of this event (moved here from Feedback - see report). */
    public double getAverageRating() {
        if (feedbackCount == 0) {
            return 0.0;
        }
        int total = 0;
        for (int i = 0; i < feedbackCount; i++) {
            total += feedbacks[i].getRating();
        }
        return (double) total / feedbackCount;          // type casting int -> double
    }

    public int getOccupancyPercent() {
        return (int) ((double) seatsTaken / capacity * 100);   // type casting double -> int
    }

    public String getSummary() {
        String price;
        if (isPaid()) {
            price = "Rs " + fee;
        } else {
            price = "Free";
        }
        return "#" + eventId + " " + title + " | " + startTime + " @ " + venue.getName()
             + " | " + price + " | seats " + seatsTaken + "/" + capacity + " | " + status;
    }

    // ---------- getters / setters ----------

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getHoursToStart() { return hoursToStart; }
    public void setHoursToStart(int hoursToStart) { this.hoursToStart = hoursToStart; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getSeatsTaken() { return seatsTaken; }

    public double getFee() { return fee; }
    public void setFee(double fee) { this.fee = fee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public int getFeedbackCount() { return feedbackCount; }
}

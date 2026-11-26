package model;

/**
 * Links a Student to an Event (FR-06, FR-07, FR-09).
 * Status: PENDING, CONFIRMED, WAITLISTED, CANCELLED.
 * COMPOSITION: a Registration owns its Ticket and its Attendance - they are created
 * inside this class and removed together with it.
 */
public class Registration {

    private int regId;
    private Student student;          // association "makes"
    private Event event;              // association "has"
    private String status;
    private int waitlistPosition;
    private Ticket ticket;            // composition (part)
    private Attendance attendance;    // composition (part)

    public Registration(int regId, Student student, Event event) {
        this.regId = regId;
        this.student = student;
        this.event = event;
        this.status = "PENDING";
        this.waitlistPosition = 0;
    }

    public void confirm() {
        status = "CONFIRMED";
        waitlistPosition = 0;
    }

    /** FR-07: put this registration on the waitlist at the given position. */
    public int joinWaitlist(int position) {
        status = "WAITLISTED";
        waitlistPosition = position;
        return waitlistPosition;
    }

    /** FR-09: cancels the registration; its parts (ticket, attendance) die with it. */
    public boolean cancel() {
        if (status.equals("CANCELLED")) {
            return false;
        }
        status = "CANCELLED";
        waitlistPosition = 0;
        ticket = null;
        attendance = null;
        return true;
    }

    /** FR-06: the whole creates its part. */
    public Ticket generateTicket() {
        if (status.equals("CONFIRMED") && ticket == null) {
            ticket = new Ticket(regId * 10, regId, event.getStartTime());
        }
        return ticket;
    }

    /** FR-10: the whole creates its Attendance part when its ticket is scanned. */
    public boolean recordAttendance(String qr, String method) {
        if (attendance == null) {
            attendance = new Attendance(regId * 10 + 1, method);
        }
        return attendance.record(qr, ticket);
    }

    public int getRegId() { return regId; }
    public void setRegId(int regId) { this.regId = regId; }

    public Student getStudent() { return student; }
    public Event getEvent() { return event; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getWaitlistPosition() { return waitlistPosition; }
    public void setWaitlistPosition(int waitlistPosition) { this.waitlistPosition = waitlistPosition; }

    public Ticket getTicket() { return ticket; }
    public Attendance getAttendance() { return attendance; }
}

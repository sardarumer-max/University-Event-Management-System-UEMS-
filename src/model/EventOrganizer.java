package model;

/** Event Organizer - creates proposals, marks attendance, issues certificates (FR-02, FR-10, FR-12). */
public class EventOrganizer extends User {

    private String designation;
    private String phone;
    private String societyName;
    private boolean active;

    public EventOrganizer(int userId, String name, String email, String password,
                          String designation, String phone, String societyName) {
        super(userId, name, email, password);
        this.designation = designation;
        this.phone = phone;
        this.societyName = societyName;
        this.active = true;
    }

    @Override
    public String getRole() {
        return "Event Organizer";
    }

    @Override
    public String getDashboard() {
        return "ORGANIZER DASHBOARD\n"
             + "  " + designation + ", " + societyName + "\n"
             + "  Menu: My proposals & approval status | Participant lists | Attendance scanner";
    }

    /**
     * FR-02 / UC-07: create a proposal. Includes UC-08 (check venue availability).
     * Returns the new Event in PENDING state, or null if it cannot be submitted.
     */
    public Event createProposal(int eventId, String title, String startTime, int hoursToStart,
                                int capacity, double fee, Venue venue) {
        if (!active) {
            System.out.println("  Your organizer account is not active.");
            return null;
        }
        if (!venue.isAvailable(startTime)) {                       // UC-08
            System.out.println("  Venue " + venue.getName() + " is already booked at " + startTime + ".");
            return null;
        }
        Event e = new Event(eventId, title, startTime, hoursToStart, capacity, fee, venue);
        e.setOrganizerName(getName());
        if (!e.submit()) {                                         // validate() + status PENDING
            System.out.println("  Proposal not valid: " + e.getValidationMessage());
            return null;
        }
        venue.hold(eventId, startTime);
        return e;
    }

    /** FR-10 / UC-11: scan a QR code and record attendance (array is passed). */
    public boolean markAttendance(Registration[] regs, int count, Event event, String qr) {
        for (int i = 0; i < count; i++) {
            Registration r = regs[i];
            if (r.getEvent() == event && r.getStatus().equals("CONFIRMED") && r.getTicket() != null
                    && r.getTicket().getQrCode().equals(qr)) {
                return r.recordAttendance(qr, "QR scan");
            }
        }
        return false;
    }

    /** FR-12 / UC-12: prints a certificate for every participant marked present. */
    public int issueCertificates(Registration[] regs, int count, Event event) {
        int issued = 0;
        for (int i = 0; i < count; i++) {
            Attendance a = regs[i].getAttendance();
            if (regs[i].getEvent() == event && a != null && a.isPresent()) {
                System.out.println("  " + a.generateCertificate(regs[i].getStudent().getName(), event.getTitle()));
                issued++;
            }
        }
        return issued;
    }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getSocietyName() { return societyName; }
    public void setSocietyName(String societyName) { this.societyName = societyName; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

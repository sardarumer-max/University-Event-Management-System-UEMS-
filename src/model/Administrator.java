package model;

/** Administrator (Student Affairs) - approves/rejects events and generates reports (FR-04, FR-14). */
public class Administrator extends User {

    private String officeName;
    private int permissionLevel;
    private int decisionsMade;

    public Administrator(int userId, String name, String email, String password,
                         String officeName, int permissionLevel) {
        super(userId, name, email, password);
        this.officeName = officeName;
        this.permissionLevel = permissionLevel;
        this.decisionsMade = 0;
    }

    @Override
    public String getRole() {
        return "Administrator";
    }

    @Override
    public String getDashboard() {
        return "ADMIN DASHBOARD - " + officeName + " (permission level " + permissionLevel + ")\n"
             + "  Menu: Pending proposals | Venue schedule | Reports";
    }

    /** FR-04 / UC-09: approve a pending proposal and publish it. */
    public boolean approveEvent(Event e, String remarks) {
        if (e == null || !e.getStatus().equals("PENDING") || permissionLevel < 1) {
            return false;
        }
        e.setRemarks(remarks);
        e.publish();                     // PENDING -> OPEN
        decisionsMade++;
        return true;
    }

    /** FR-04 / UC-09: reject a pending proposal and free its venue slot. */
    public boolean rejectEvent(Event e, String remarks) {
        if (e == null || !e.getStatus().equals("PENDING")) {
            return false;
        }
        e.setRemarks(remarks);
        e.setStatus("REJECTED");
        e.getVenue().release(e.getEventId());
        decisionsMade++;
        return true;
    }

    /** FR-14: participation and revenue report (array is passed). */
    public String generateReport(Event[] events, int count) {
        String report = "PARTICIPATION & REVENUE REPORT\n";
        double totalRevenue = 0;
        int totalSeats = 0;
        for (int i = 0; i < count; i++) {
            double revenue = events[i].getSeatsTaken() * events[i].getFee();   // arithmetic
            totalRevenue = totalRevenue + revenue;
            totalSeats += events[i].getSeatsTaken();
            report += "  " + events[i].getTitle() + ": " + events[i].getSeatsTaken() + "/"
                    + events[i].getCapacity() + " seats (" + events[i].getOccupancyPercent()
                    + "%), revenue Rs " + revenue + ", rating " + events[i].getAverageRating() + "\n";
        }
        report += "  TOTAL: " + totalSeats + " participants, Rs " + totalRevenue;
        return report;
    }

    public String getOfficeName() { return officeName; }
    public void setOfficeName(String officeName) { this.officeName = officeName; }

    public int getPermissionLevel() { return permissionLevel; }
    public void setPermissionLevel(int permissionLevel) { this.permissionLevel = permissionLevel; }

    public int getDecisionsMade() { return decisionsMade; }
}

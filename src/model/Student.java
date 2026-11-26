package model;

/** Student - registers, pays, cancels and gives feedback (FR-05..FR-13). */
public class Student extends User {

    private String rollNo;
    private String department;
    private int semester;
    private String phone;

    // Constructor 1: all details
    public Student(int userId, String name, String email, String password,
                   String rollNo, String department, int semester, String phone) {
        super(userId, name, email, password);          // super in constructor
        this.rollNo = rollNo;
        this.department = department;
        this.semester = semester;
        this.phone = phone;
    }

    // Constructor 2 (overloaded): new student whose department/semester are not known yet
    public Student(int userId, String name, String email, String password, String rollNo) {
        this(userId, name, email, password, rollNo, "Not set", 1, "Not set");
    }

    // ---------- overridden abstract methods ----------

    @Override
    public String getRole() {
        return "Student";
    }

    @Override
    public String getDashboard() {
        return "STUDENT DASHBOARD\n"
             + "  Roll no: " + rollNo + " | " + department + " | Semester " + semester + "\n"
             + "  Menu: Browse events | My registrations | E-tickets | Certificates | Feedback";
    }

    @Override
    public String toString() {
        return super.toString() + " Roll no: " + rollNo;   // super in a method
    }

    // ---------- operations from the class diagram ----------

    /** FR-05: search by keyword in the title (array is passed to the method). */
    public int searchEvents(Event[] events, int count, String keyword) {
        int found = 0;
        String key = keyword.toLowerCase();
        for (int i = 0; i < count; i++) {
            boolean published = events[i].getStatus().equals("OPEN") || events[i].getStatus().equals("FULL");
            if (published && events[i].getTitle().toLowerCase().contains(key)) {
                System.out.println("  " + events[i].getSummary());
                found++;
            }
        }
        return found;
    }

    /** Overloaded version: search by maximum fee instead of keyword. */
    public int searchEvents(Event[] events, int count, double maxFee) {
        int found = 0;
        for (int i = 0; i < count; i++) {
            boolean published = events[i].getStatus().equals("OPEN") || events[i].getStatus().equals("FULL");
            if (published && events[i].getFee() <= maxFee) {
                System.out.println("  " + events[i].getSummary());
                found++;
            }
        }
        return found;
    }

    /** FR-13: rate an attended event (1-5 stars). */
    public boolean submitFeedback(Event event, int feedbackId, int stars, String comment) {
        Feedback f = new Feedback(feedbackId, stars, comment, getName());
        if (!f.validate()) {
            System.out.println("  Feedback rejected: " + f.getValidationMessage());
            return false;
        }
        f.save();
        event.addFeedback(f);
        return true;
    }

    // ---------- getters / setters ----------

    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}

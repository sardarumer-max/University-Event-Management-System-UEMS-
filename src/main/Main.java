package main;

import java.util.Scanner;

import model.Administrator;
import model.Event;
import model.EventOrganizer;
import model.PaymentGateway;
import model.Registration;
import model.RegistrationManager;
import model.Student;
import model.User;
import model.Venue;

/**
 * University Event Management System (UEMS) - console version.
 * Runs the text menu and connects the user to the model classes.
 */
public class Main {

    private static Scanner sc = new Scanner(System.in);

    // ---------- data kept in arrays ----------
    private static Venue[] venues = new Venue[3];
    private static Student[] students = new Student[3];
    private static User[] users = new User[5];                 // parent-type array (polymorphism)
    private static Event[] events = new Event[20];
    private static int eventCount = 0;
    private static int nextEventId = 1;
    private static int nextFeedbackId = 1;

    private static EventOrganizer organizer;
    private static Administrator admin;
    private static RegistrationManager manager;

    public static void main(String[] args) {
        loadSampleData();
        System.out.println("==============================================");
        System.out.println("  UNIVERSITY EVENT MANAGEMENT SYSTEM (UEMS)");
        System.out.println("==============================================");

        int choice;
        do {
            printMenu();
            choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: loginAndDashboard(); break;
                case 2: browseEvents(); break;
                case 3: createProposal(); break;
                case 4: approveOrReject(); break;
                case 5: registerForEvent(); break;
                case 6: retryPayment(); break;
                case 7: cancelRegistration(); break;
                case 8: myRegistrations(); break;
                case 9: markAttendance(); break;
                case 10: submitFeedback(); break;
                case 11: System.out.println(admin.generateReport(events, eventCount)); break;
                case 0: System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid choice, please try again.");
            }
        } while (choice != 0);
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("---------------- MAIN MENU ----------------");
        System.out.println(" 1. Login & view dashboard        (UC-01)");
        System.out.println(" 2. Browse / search events        (UC-02)");
        System.out.println(" 3. Create event proposal         (UC-07)");
        System.out.println(" 4. Approve / reject proposal     (UC-09)");
        System.out.println(" 5. Register for event            (UC-03)");
        System.out.println(" 6. Retry payment                 (UC-04)");
        System.out.println(" 7. Cancel registration           (UC-06)");
        System.out.println(" 8. My registrations & e-tickets");
        System.out.println(" 9. Mark attendance / certificates (UC-11, UC-12)");
        System.out.println("10. Submit feedback               (UC-13)");
        System.out.println("11. Participation & revenue report (UC-14)");
        System.out.println(" 0. Exit");
    }

    // ================= menu operations =================

    /** UC-01: one loop over a User[] array; getRole()/getDashboard() run the child version. */
    private static void loginAndDashboard() {
        String email = readLine("E-mail: ");
        String pwd = readLine("Password: ");
        for (int i = 0; i < users.length; i++) {
            User u = users[i];
            if (u.login(email, pwd)) {
                System.out.println("Login successful as " + u.getRole() + ".");
                System.out.println(u.getDashboard());          // dynamic binding
                u.logout();
                return;
            }
        }
        System.out.println("Invalid e-mail or password.");
    }

    /** UC-02 */
    private static void browseEvents() {
        Student s = students[0];
        char mode = readLine("Search by (K)eyword, (F)ee limit or (A)ll? ").toUpperCase().charAt(0);
        int found;
        switch (mode) {
            case 'K':
                found = s.searchEvents(events, eventCount, readLine("Keyword: "));
                break;
            case 'F':
                found = s.searchEvents(events, eventCount, readDouble("Maximum fee (Rs): "));
                break;
            default:
                found = s.searchEvents(events, eventCount, "");
        }
        System.out.println(found + " published event(s) found.");
    }

    /** UC-07 (includes UC-08 Check Venue Availability) */
    private static void createProposal() {
        System.out.println("Organizer: " + organizer.getName() + " (" + organizer.getSocietyName() + ")");
        String title = readLine("Event title: ");
        String start = readLine("Date & time (yyyy-mm-dd hh:mm): ");
        int hours = readInt("Hours from now until the event: ");
        int capacity = readInt("Capacity: ");
        double fee = readDouble("Fee in Rs (0 for free): ");
        for (int i = 0; i < venues.length; i++) {
            System.out.println("  " + (i + 1) + ". " + venues[i].getName() + " (capacity " + venues[i].getCapacity() + ")");
        }
        int v = readInt("Choose venue: ");
        if (v < 1 || v > venues.length) {
            System.out.println("Invalid venue.");
            return;
        }
        Event e = organizer.createProposal(nextEventId, title, start, hours, capacity, fee, venues[v - 1]);
        if (e != null) {
            events[eventCount] = e;
            eventCount++;
            nextEventId++;
            System.out.println("Proposal submitted: " + e.getSummary());
            System.out.println("It is waiting for administrator approval.");
        }
    }

    /** UC-09 */
    private static void approveOrReject() {
        int pending = 0;
        for (int i = 0; i < eventCount; i++) {
            if (events[i].getStatus().equals("PENDING")) {
                System.out.println("  " + events[i].getSummary() + " | by " + events[i].getOrganizerName());
                pending++;
            }
        }
        if (pending == 0) {
            System.out.println("No pending proposals.");
            return;
        }
        Event e = manager.findEvent(events, eventCount, readInt("Event id: "));
        char decision = readLine("(A)pprove or (R)eject? ").toUpperCase().charAt(0);
        String remarks = readLine("Remarks: ");
        boolean done;
        if (decision == 'A') {
            done = admin.approveEvent(e, remarks);
        } else if (decision == 'R') {
            done = admin.rejectEvent(e, remarks);
        } else {
            System.out.println("Unknown decision.");
            return;
        }
        if (done) {
            System.out.println("Event is now " + e.getStatus() + ". Organizer notified: \"" + remarks + "\"");
        } else {
            System.out.println("Decision failed - the event is not pending.");
        }
    }

    /** UC-03 (extended by UC-04 Make Payment and UC-05 Join Waitlist) */
    private static void registerForEvent() {
        Student s = askStudent();
        if (s == null) {
            return;
        }
        Event e = manager.findEvent(events, eventCount, readInt("Event id: "));
        if (e == null) {
            System.out.println("Event not found.");
            return;
        }
        double wallet = 0;
        if (e.isPaid() && e.hasSeats()) {
            wallet = readDouble("Fee is Rs " + e.getFee() + ". Wallet balance to pay from (Rs): ");
        }
        Registration r = manager.register(s, e, wallet);
        if (r != null) {
            System.out.println("Result: Registration " + r.getRegId() + " is " + r.getStatus() + ".");
        }
    }

    /** UC-04 */
    private static void retryPayment() {
        int regId = readInt("Registration id: ");
        double wallet = readDouble("Wallet balance to pay from (Rs): ");
        manager.retryPayment(regId, wallet);
    }

    /** UC-06 */
    private static void cancelRegistration() {
        int regId = readInt("Registration id to cancel: ");
        char sure = readLine("Are you sure? (Y/N): ").toUpperCase().charAt(0);
        if (sure == 'Y' && manager.cancel(regId)) {
            System.out.println("Registration " + regId + " cancelled.");
        } else {
            System.out.println("Nothing was cancelled.");
        }
    }

    private static void myRegistrations() {
        Student s = askStudent();
        if (s != null && manager.printRegistrations(s) == 0) {
            System.out.println("No registrations yet.");
        }
    }

    /** UC-11 and UC-12 */
    private static void markAttendance() {
        Event e = manager.findEvent(events, eventCount, readInt("Event id: "));
        if (e == null) {
            System.out.println("Event not found.");
            return;
        }
        String qr = readLine("Scan QR code: ");
        if (organizer.markAttendance(manager.getRegistrations(), manager.getRegCount(), e, qr)) {
            System.out.println("Attendance recorded.");
        } else {
            System.out.println("Invalid or already used ticket.");
        }
        char issue = readLine("Issue certificates now? (Y/N): ").toUpperCase().charAt(0);
        if (issue == 'Y') {
            int n = organizer.issueCertificates(manager.getRegistrations(), manager.getRegCount(), e);
            System.out.println(n + " certificate(s) issued.");
        }
    }

    /** UC-13: only a student marked present may rate the event. */
    private static void submitFeedback() {
        Registration r = manager.findRegistration(readInt("Your registration id: "));
        if (r == null || r.getAttendance() == null || !r.getAttendance().isPresent()) {
            System.out.println("Feedback is allowed only for events you attended.");
            return;
        }
        int stars = readInt("Rating (1-5): ");
        String comment = readLine("Comment: ");
        if (r.getStudent().submitFeedback(r.getEvent(), nextFeedbackId, stars, comment)) {
            nextFeedbackId++;
            System.out.println("Thank you! Average rating of '" + r.getEvent().getTitle() + "' is now "
                    + r.getEvent().getAverageRating());
        }
    }

    // ================= helpers =================

    private static Student askStudent() {
        String roll = readLine("Your roll number: ");
        Student s = findStudent(students, roll);
        if (s == null) {
            System.out.println("Student " + roll + " not found.");
        }
        return s;
    }

    /** Array passed to a method: linear search by roll number. */
    private static Student findStudent(Student[] list, String rollNo) {
        for (int i = 0; i < list.length; i++) {
            if (list[i] != null && list[i].getRollNo().equalsIgnoreCase(rollNo)) {
                return list[i];
            }
        }
        return null;
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        if (line.length() == 0) {
            return " ";            // so charAt(0) never fails
        }
        return line;
    }

    /** Returns -1 if the user did not type a whole number. */
    private static int readInt(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextInt()) {
            sc.nextLine();
            return -1;
        }
        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }

    private static double readDouble(String prompt) {
        System.out.print(prompt);
        if (!sc.hasNextDouble()) {
            sc.nextLine();
            return -1;
        }
        double value = sc.nextDouble();
        sc.nextLine();
        return value;
    }

    private static void loadSampleData() {
        PaymentGateway gateway = new PaymentGateway("UEMS-MERCHANT-01", "https://pay.example/api", 30);
        manager = new RegistrationManager(gateway, 3, 24);

        venues[0] = new Venue(1, "Main Auditorium", 300, "Admin Block");
        venues[1] = new Venue(2, "Seminar Hall", 80, "CS Block, 1st floor");
        venues[2] = new Venue(3, "CS Lab 2", 40, "CS Block, ground floor");

        students[0] = new Student(1, "Ali Khan", "ali@uems.edu.pk", "ali123", "24P-0101", "CS", 5, "0300-1111111");
        students[1] = new Student(2, "Sara Ahmed", "sara@uems.edu.pk", "sara123", "24P-0102", "SE", 3, "0300-2222222");
        students[2] = new Student(3, "Hamza Tariq", "hamza@uems.edu.pk", "hamza123", "24P-0103"); // overloaded constructor
        students[2].setPhone("0300-3333333");
        organizer = new EventOrganizer(4, "Ayesha Noor", "ayesha@uems.edu.pk", "org123",
                "President", "0300-4444444", "Computing Society");
        admin = new Administrator(5, "Imran Shah", "admin@uems.edu.pk", "admin123", "Student Affairs Office", 2);

        users[0] = students[0];            // child objects stored in parent-type array
        users[1] = students[1];
        users[2] = students[2];
        users[3] = organizer;
        users[4] = admin;

        addSeedEvent(organizer.createProposal(nextEventId, "Coding Competition", "2026-12-10 10:00", 360, 2, 0, venues[2]), true);
        addSeedEvent(organizer.createProposal(nextEventId, "AI Workshop", "2026-12-12 14:00", 408, 30, 500, venues[1]), true);
        addSeedEvent(organizer.createProposal(nextEventId, "Career Fair", "2026-11-26 11:00", 20, 150, 0, venues[0]), true);
        addSeedEvent(organizer.createProposal(nextEventId, "Sports Gala", "2026-12-15 09:00", 480, 200, 200, venues[0]), false);
    }

    private static void addSeedEvent(Event e, boolean approve) {
        events[eventCount] = e;
        eventCount++;
        nextEventId++;
        if (approve) {
            admin.approveEvent(e, "Approved");
        }
    }
}

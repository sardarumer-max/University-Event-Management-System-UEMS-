package model;

/** Attendance record created when a ticket is scanned (FR-10). Part of a Registration (composition). */
public class Attendance {

    private int attendanceId;
    private String method;
    private boolean present;

    public Attendance(int attendanceId, String method) {
        this.attendanceId = attendanceId;
        this.method = method;
        this.present = false;
    }

    /** Records presence if the ticket accepts the scanned QR code (a used ticket is refused). */
    public boolean record(String qr, Ticket ticket) {
        if (ticket != null && ticket.validate(qr)) {
            ticket.markUsed();
            present = true;
            return true;
        }
        return false;
    }

    public boolean isPresent() {
        return present;
    }

    /** FR-12: certificate text for a participant marked present. */
    public String generateCertificate(String studentName, String eventTitle) {
        if (!present) {
            return "No certificate: " + studentName + " was not marked present.";
        }
        return "CERTIFICATE #" + attendanceId + ": This certifies that " + studentName
             + " participated in \"" + eventTitle + "\".";
    }

    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}

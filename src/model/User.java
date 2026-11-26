package model;

/**
 * Abstract parent of every person who has an account (FR-01).
 * Children: Student, EventOrganizer, Administrator (hierarchical inheritance).
 */
public abstract class User {

    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private boolean loggedIn;

    public User(int userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = hash(password);
        this.loggedIn = false;
    }

    // ---------- concrete methods (shared by all children) ----------

    /** FR-01: returns true when the e-mail and password match this account. */
    public boolean login(String email, String pwd) {
        if (this.email.equalsIgnoreCase(email) && this.passwordHash.equals(hash(pwd))) {
            loggedIn = true;
        } else {
            loggedIn = false;
        }
        return loggedIn;
    }

    public void logout() {
        loggedIn = false;
    }

    /** Basic account details; children extend it with super.toString(). */
    @Override
    public String toString() {
        return "[" + userId + "] " + name + " <" + email + ">";
    }

    // ---------- abstract methods: each child must override them ----------

    public abstract String getDashboard();

    public abstract String getRole();

    /** Very small "hash" so the plain password is never stored. */
    private String hash(String pwd) {
        return "H" + pwd.length() + pwd.hashCode();
    }

    // ---------- getters / setters ----------

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isLoggedIn() { return loggedIn; }

    public void setPassword(String newPassword) { this.passwordHash = hash(newPassword); }
}

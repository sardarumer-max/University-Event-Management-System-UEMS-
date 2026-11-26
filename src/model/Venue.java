package model;

/** Venue with its own booking rules (FR-03): no two events in the same slot. */
public class Venue {

    private int venueId;
    private String name;
    private int capacity;
    private String location;
    private String[] bookedSlots = new String[10];
    private int[] bookedEventIds = new int[10];
    private int bookedCount = 0;

    public Venue(int venueId, String name, int capacity, String location) {
        this.venueId = venueId;
        this.name = name;
        this.capacity = capacity;
        this.location = location;
    }

    /** UC-08: true when no other event holds this slot. */
    public boolean isAvailable(String slot) {
        for (int i = 0; i < bookedCount; i++) {
            if (bookedSlots[i].equalsIgnoreCase(slot)) {
                return false;
            }
        }
        return true;
    }

    public boolean hold(int eventId, String slot) {
        if (!isAvailable(slot) || bookedCount >= bookedSlots.length) {
            return false;
        }
        bookedSlots[bookedCount] = slot;
        bookedEventIds[bookedCount] = eventId;
        bookedCount++;
        return true;
    }

    /** Frees the slot of a rejected or cancelled event (last entry moves into the gap). */
    public void release(int eventId) {
        for (int i = 0; i < bookedCount; i++) {
            if (bookedEventIds[i] == eventId) {
                bookedSlots[i] = bookedSlots[bookedCount - 1];
                bookedEventIds[i] = bookedEventIds[bookedCount - 1];
                bookedCount--;
                return;
            }
        }
    }

    public int getVenueId() { return venueId; }
    public void setVenueId(int venueId) { this.venueId = venueId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getBookedCount() { return bookedCount; }
}

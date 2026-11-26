package model;

/**
 * Capability interface: "this object can check its own data before it is saved".
 * Implemented by two unrelated classes, Event (FR-02) and Feedback (FR-13).
 */
public interface Validatable {

    boolean validate();

    String getValidationMessage();
}

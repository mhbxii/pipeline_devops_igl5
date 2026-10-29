package tn.esprit.eventsproject.services;

public class LogisticsEventNotFoundException extends RuntimeException {
    public LogisticsEventNotFoundException() {
        super("Event not found for logistics assignment");
    }
}

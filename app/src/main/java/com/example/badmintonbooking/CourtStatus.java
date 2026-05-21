package com.example.badmintonbooking;

public class CourtStatus {
    public static final int STATUS_EMPTY = 0;
    public static final int STATUS_PENDING = 1;
    public static final int STATUS_BOOKED = 2;

    private int number;
    private int status;

    public CourtStatus(int number, int status) {
        this.number = number;
        this.status = status;
    }

    public int getNumber() { return number; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}

package com.example.badmintonbooking;

import java.util.ArrayList;

public class AdminBooking {
    public String id;
    public String branchName;
    public String date;
    public ArrayList<String> selectedTimes;
    public long totalPrice;
    public String status;
    public String bookingCode;

    public AdminBooking(String id, String bookingCode, String branchName, String date,
                        ArrayList<String> selectedTimes, long totalPrice, String status) {
        this.id = id;
        this.bookingCode = bookingCode;
        this.branchName = branchName;
        this.date = date;
        this.selectedTimes = selectedTimes;
        this.totalPrice = totalPrice;
        this.status = status;
    }
}
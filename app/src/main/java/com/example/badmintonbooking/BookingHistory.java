package com.example.badmintonbooking;

public class BookingHistory {
    private String bookingId;
    private String bookingCode;
    private String branchName;
    private String date;
    private String slotsText;
    private String paymentMethod;
    private String status;
    private String totalPriceFormatted;

    public BookingHistory(String bookingId, String bookingCode, String branchName, String date,
                          String slotsText, String paymentMethod, String status,
                          String totalPriceFormatted) {
        this.bookingId = bookingId;
        this.bookingCode = bookingCode;
        this.branchName = branchName;
        this.date = date;
        this.slotsText = slotsText;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.totalPriceFormatted = totalPriceFormatted;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getDate() {
        return date;
    }

    public String getSlotsText() {
        return slotsText;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getTotalPriceFormatted() {
        return totalPriceFormatted;
    }
}
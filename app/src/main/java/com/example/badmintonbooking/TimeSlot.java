package com.example.badmintonbooking;

public class TimeSlot {
    private String time;
    private boolean isSelected;

    public TimeSlot(String time, boolean isSelected) {
        this.time = time;
        this.isSelected = isSelected;
    }

    public String getTime() { return time; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}

package com.example.badmintonbooking;

public class Court {
    private String name;
    private String location;
    private long price;

    public Court() {
    }

    public Court(String name, String location, long price) {
        this.name = name;
        this.location = location;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public long getPrice() {
        return price;
    }
}
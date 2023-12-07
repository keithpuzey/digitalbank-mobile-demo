package xyz.digitalbank.demo.Model;

public class AccountInfo {
    private int id;
    private String name;
    private double currentBalance;

    public AccountInfo(int id, String name, double currentBalance) {
        this.id = id;
        this.name = name;
        this.currentBalance = currentBalance;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return name + " - " + currentBalance;
    }

    // Override the toString method to return the display name
    @Override
    public String toString() {
        return getDisplayName();
    }
}
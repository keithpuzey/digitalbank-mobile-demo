package xyz.digitalbank.demo.Model;



public class AccountInfo {
    private int id;
    private String accountName;
    private double currentBalance;
    private String accountTypeName;
    private int accountNumber;


    public AccountInfo(int id, String accountName, double currentBalance, String accountTypeName, int accountNumber) {
        this.id = id;
        this.accountName = accountName;
        this.currentBalance = currentBalance;
        this.accountTypeName = accountTypeName;
        this.accountNumber = accountNumber;
    }

    // Add getters for the new field
    public String getAccountTypeName() {
        return accountTypeName;
    }
    public String getAccountName() {
        return accountName;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return accountName + " - " + currentBalance;
    }
    @Override
    public String toString() {
        return getDisplayName();
    }
}
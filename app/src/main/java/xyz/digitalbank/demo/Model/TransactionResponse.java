package xyz.digitalbank.demo.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("description")
    private String description;

    @SerializedName("amount")
    private double amount;

    @SerializedName("runningBalance")
    private double runningBalance;

    @SerializedName("transactionNumber")
    private int transactionNumber;

    @SerializedName("transactionDate")
    private String transactionDate;

    @SerializedName("transactionType")
    private TransactionType transactionType;

    @SerializedName("transactionState")
    private TransactionState transactionState;

    @SerializedName("transactionCategory")
    private TransactionCategory transactionCategory;

    // Constructors, getters, and setters...

    // Inner classes for nested objects

    public static class TransactionType {
        @SerializedName("id")
        private int id;

        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        @SerializedName("category")
        private String category;

        // Constructors, getters, and setters...
    }

    public static class TransactionState {
        @SerializedName("id")
        private int id;

        @SerializedName("code")
        private String code;

        @SerializedName("name")
        private String name;

        // Constructors, getters, and setters...
    }

    public static class TransactionCategory {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("code")
        private String code;

        // Constructors, getters, and setters...
    }

    // Constructors, getters, and setters...

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(double runningBalance) {
        this.runningBalance = runningBalance;
    }

    public int getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(int transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public TransactionState getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(TransactionState transactionState) {
        this.transactionState = transactionState;
    }

    public TransactionCategory getTransactionCategory() {
        return transactionCategory;
    }

    public void setTransactionCategory(TransactionCategory transactionCategory) {
        this.transactionCategory = transactionCategory;
    }
}

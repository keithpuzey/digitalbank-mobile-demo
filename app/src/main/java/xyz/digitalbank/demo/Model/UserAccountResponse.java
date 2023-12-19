package xyz.digitalbank.demo.Model;
import com.google.gson.annotations.SerializedName;

public class UserAccountResponse {

    private int id;
    private String name;
    @SerializedName("accountNumber")
    private int accountNumber;
    @SerializedName("currentBalance")
    private double currentBalance;
    @SerializedName("openingBalance")
    private double openingBalance;
    @SerializedName("interestRate")
    private double interestRate;
    @SerializedName("paymentAmount")
    private double paymentAmount;
    @SerializedName("paymentTerm")
    private int paymentTerm;
    private AccountType accountType;
    private OwnershipType ownershipType;
    private AccountStanding accountStanding;
    @SerializedName("dateOpened")
    private String dateOpened;
    @SerializedName("dateClosed")
    private String dateClosed;
    private String paymentDue;

    // Getters and setters for all fields

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(double paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public int getPaymentTerm() {
        return paymentTerm;
    }

    public void setPaymentTerm(int paymentTerm) {
        this.paymentTerm = paymentTerm;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public OwnershipType getOwnershipType() {
        return ownershipType;
    }

    public void setOwnershipType(OwnershipType ownershipType) {
        this.ownershipType = ownershipType;
    }

    public AccountStanding getAccountStanding() {
        return accountStanding;
    }

    public void setAccountStanding(AccountStanding accountStanding) {
        this.accountStanding = accountStanding;
    }

    public String getDateOpened() {
        return dateOpened;
    }

    public void setDateOpened(String dateOpened) {
        this.dateOpened = dateOpened;
    }

    public String getDateClosed() {
        return dateClosed;
    }

    public void setDateClosed(String dateClosed) {
        this.dateClosed = dateClosed;
    }

    public String getPaymentDue() {
        return paymentDue;
    }

    public void setPaymentDue(String paymentDue) {
        this.paymentDue = paymentDue;
    }

    // Nested classes representing objects within UserAccountResponse

    public static class AccountType {
        private int id;
        private String code;
        private String category;
        private String name;
        @SerializedName("interestRate")
        private double interestRate;
        @SerializedName("minDeposit")
        private double minDeposit;
        @SerializedName("overdraftLimit")
        private int overdraftLimit;
        @SerializedName("overdraftFee")
        private int overdraftFee;
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class OwnershipType {
        private int id;
        private String code;
        private String name;

        // Getters and setters for all fields
    }

    public static class AccountStanding {
        private int id;
        private String code;
        private String name;

        // Getters and setters for all fields
    }
}

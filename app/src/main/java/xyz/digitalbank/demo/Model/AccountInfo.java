

package xyz.digitalbank.demo.Model;

public class AccountInfo {
    private int id;
    private String name;
    private String currentBalance;

    public AccountInfo(int id, String name, String currentBalance) {
        this.id = id;
        this.name = name;
        this.currentBalance = currentBalance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }
}

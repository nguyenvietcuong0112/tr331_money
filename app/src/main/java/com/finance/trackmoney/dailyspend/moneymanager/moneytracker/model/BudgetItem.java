package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model;

public class BudgetItem {
    private String name;
    private double totalAmount;
    private double spentAmount;
    private int color;

    public BudgetItem(String name, double totalAmount) {
        this.name = name;
        this.totalAmount = totalAmount;
        this.spentAmount = 0;
        this.color = generateRandomColor();
    }

    private int generateRandomColor() {
        // Generate a random color for the budget visualization
        return android.graphics.Color.rgb(
                (int)(Math.random() * 200 + 55),
                (int)(Math.random() * 200 + 55),
                (int)(Math.random() * 200 + 55)
        );
    }

    public void addExpense(double amount) {
        if (spentAmount + amount > totalAmount) {
            this.spentAmount = totalAmount; // Đảm bảo không vượt quá tổng ngân sách
        } else {
            this.spentAmount += amount;
        }
    }
    public double getRemainingAmount() {
        return totalAmount - spentAmount;
    }

    public double getProgress() {
        if (totalAmount == 0) return 0;
        return (spentAmount / totalAmount) * 100;
    }

    // Getters

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }
    public String getName() { return name; }
    public double getTotalAmount() { return totalAmount; }
    public double getSpentAmount() { return spentAmount; }
    public int getColor() { return color; }
}
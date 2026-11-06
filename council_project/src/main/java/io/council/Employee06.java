package io.council;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Employee06 {

    public enum Department { ENGINEERING, SALES, HR, FINANCE, MARKETING }
    public enum Level { JUNIOR, MID, SENIOR, LEAD, MANAGER }

    private final String employeeId;
    private final String name;
    private Department department;
    private Level level;
    private double baseSalary;
    private final List<Double> bonuses;
    private boolean active;
    private int yearsOfService;

    public Employee06(String employeeId, String name, Department department, Level level, double baseSalary) {
        if (employeeId == null || employeeId.isBlank()) throw new IllegalArgumentException("Employee ID required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
        if (department == null) throw new IllegalArgumentException("Department required");
        if (level == null) throw new IllegalArgumentException("Level required");
        if (baseSalary < 0) throw new IllegalArgumentException("Salary cannot be negative");
        this.employeeId = employeeId;
        this.name = name;
        this.department = department;
        this.level = level;
        this.baseSalary = baseSalary;
        this.bonuses = new ArrayList<>();
        this.active = true;
        this.yearsOfService = 0;
    }

    public void addBonus(double amount) {
        if (!active) throw new IllegalStateException("Inactive employee");
        if (amount <= 0) throw new IllegalArgumentException("Bonus must be positive");
        bonuses.add(amount);
    }

    public double totalCompensation() {
        double bonusSum = bonuses.stream().mapToDouble(Double::doubleValue).sum();
        return baseSalary + bonusSum;
    }

    public void promote(Level newLevel) {
        if (!active) throw new IllegalStateException("Inactive employee");
        if (newLevel == null) throw new IllegalArgumentException("Level required");
        if (newLevel.ordinal() <= level.ordinal()) throw new IllegalArgumentException("Promotion must be to higher level");
        this.level = newLevel;
        this.baseSalary *= 1.10;
    }

    public void transfer(Department newDept) {
        if (!active) throw new IllegalStateException("Inactive employee");
        if (newDept == null) throw new IllegalArgumentException("Department required");
        if (newDept == this.department) throw new IllegalArgumentException("Already in that department");
        this.department = newDept;
    }

    public void incrementYears() {
        if (!active) throw new IllegalStateException("Inactive employee");
        this.yearsOfService++;
    }

    public void deactivate() { this.active = false; }

    public double calculateAnnualRaise() {
        if (!active) return 0;
        double rate = switch (level) {
            case JUNIOR -> 0.05;
            case MID -> 0.06;
            case SENIOR -> 0.07;
            case LEAD -> 0.08;
            case MANAGER -> 0.10;
        };
        if (yearsOfService >= 5) rate += 0.02;
        return baseSalary * rate;
    }

    public boolean isEligibleForBonus() {
        return active && yearsOfService >= 1 && level != Level.JUNIOR;
    }

    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public Department getDepartment() { return department; }
    public Level getLevel() { return level; }
    public double getBaseSalary() { return baseSalary; }
    public List<Double> getBonuses() { return Collections.unmodifiableList(bonuses); }
    public boolean isActive() { return active; }
    public int getYearsOfService() { return yearsOfService; }
}

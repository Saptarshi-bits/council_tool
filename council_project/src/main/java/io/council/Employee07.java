package io.council;

import java.util.ArrayList;
import java.util.List;

public class Employee07 {

    public enum Department { ENGINEERING, MARKETING, SALES, HR, FINANCE }
    public enum Role { JUNIOR, MID, SENIOR, LEAD, MANAGER, DIRECTOR }

    private String id;
    private String name;
    private Department department;
    private Role role;
    private double baseSalary;
    private double bonusPercent;
    private int yearsOfExperience;
    private boolean active;
    private List<String> promotionHistory;

    public Employee07(String id, String name, Department department, Role role, double baseSalary) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Employee ID cannot be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name cannot be blank");
        if (baseSalary < 0) throw new IllegalArgumentException("Salary cannot be negative");
        this.id = id;
        this.name = name;
        this.department = department;
        this.role = role;
        this.baseSalary = baseSalary;
        this.bonusPercent = 0.0;
        this.yearsOfExperience = 0;
        this.active = true;
        this.promotionHistory = new ArrayList<>();
    }

    public double calculateTotalCompensation() {
        return baseSalary + (baseSalary * bonusPercent / 100.0);
    }

    public double calculateMonthlyPay() {
        return calculateTotalCompensation() / 12.0;
    }

    public boolean promote(Role newRole) {
        if (!active) return false;
        if (newRole.ordinal() <= role.ordinal()) return false;
        promotionHistory.add(role + " -> " + newRole);
        this.role = newRole;
        this.baseSalary *= 1.10;
        return true;
    }

    public boolean transferTo(Department newDepartment) {
        if (!active) return false;
        if (newDepartment == this.department) return false;
        this.department = newDepartment;
        return true;
    }

    public void applyRaise(double percentIncrease) {
        if (!active) throw new IllegalStateException("Cannot apply raise to inactive employee");
        if (percentIncrease < 0) throw new IllegalArgumentException("Raise percent cannot be negative");
        this.baseSalary += this.baseSalary * percentIncrease / 100.0;
    }

    public void setBonus(double percent) {
        if (percent < 0) throw new IllegalArgumentException("Bonus percent cannot be negative");
        if (percent > 100) throw new IllegalArgumentException("Bonus percent cannot exceed 100");
        this.bonusPercent = percent;
    }

    public void terminate() {
        this.active = false;
        this.bonusPercent = 0.0;
    }

    public void rehire() {
        this.active = true;
    }

    public boolean isEligibleForPromotion() {
        return active && yearsOfExperience >= 2 && role != Role.DIRECTOR;
    }

    public boolean isManager() {
        return role == Role.MANAGER || role == Role.DIRECTOR;
    }

    public void incrementExperience() {
        this.yearsOfExperience++;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Department getDepartment() { return department; }
    public Role getRole() { return role; }
    public double getBaseSalary() { return baseSalary; }
    public double getBonusPercent() { return bonusPercent; }
    public int getYearsOfExperience() { return yearsOfExperience; }
    public boolean isActive() { return active; }
    public List<String> getPromotionHistory() { return new ArrayList<>(promotionHistory); }

    @Override
    public String toString() {
        return String.format("Employee07[%s, %s, %s, %s, salary=%.2f, active=%s]",
                id, name, department, role, baseSalary, active);
    }
}

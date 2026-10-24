package shop;

import java.util.*;

public class EmployeeManager {
    private List<String> names = new ArrayList<>();
    private List<String> departments = new ArrayList<>();
    private List<Double> salaries = new ArrayList<>();
    private List<Integer> vacationDays = new ArrayList<>();
    private List<Integer> sickDays = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private List<Integer> performanceScores = new ArrayList<>();
    private Map<String, List<String>> schedule = new HashMap<>();
    private Map<String, String> managers = new HashMap<>();
    private List<String> notifications = new ArrayList<>();
    private List<String> auditTrail = new ArrayList<>();
    private double bonusPool = 50000;
    private double taxRate = 0.1;
    private int hoursPerWeek = 40;

    public void hire(String name, String dept, double salary, String email, String manager,
                     int vacation, int sick, int score, String startDay, String shift) {
        names.add(name);
        departments.add(dept);
        salaries.add(salary);
        emails.add(email);
        vacationDays.add(vacation);
        sickDays.add(sick);
        performanceScores.add(score);
        managers.put(name, manager);
        schedule.computeIfAbsent(startDay, k -> new ArrayList<>()).add(name + ":" + shift);
        audit("Hired " + name);
        notifyUser(email, "Welcome " + name);
    }

    public void fire(String name) {
        int i = names.indexOf(name);
        if (i < 0) {
            return;
        }
        names.remove(i);
        departments.remove(i);
        salaries.remove(i);
        emails.remove(i);
        vacationDays.remove(i);
        sickDays.remove(i);
        performanceScores.remove(i);
        managers.remove(name);
        audit("Fired " + name);
    }

    public double grossPay(int i) {
        return salaries.get(i) / 12;
    }

    public double netPay(int i) {
        double g = grossPay(i);
        return g - g * taxRate;
    }

    public double calculateBonus(int i) {
        int s = performanceScores.get(i);
        double base = salaries.get(i);
        if (s >= 90) {
            return base * 0.20;
        } else if (s >= 75) {
            return base * 0.10;
        } else if (s >= 60) {
            return base * 0.05;
        }
        return 0;
    }

    public double totalPayroll() {
        double t = 0;
        for (int i = 0; i < names.size(); i++) {
            t += netPay(i) + calculateBonus(i) / 12;
        }
        return t;
    }

    public boolean requestVacation(String name, int days) {
        int i = names.indexOf(name);
        if (i < 0 || days <= 0) {
            return false;
        }
        if (vacationDays.get(i) < days) {
            notifyUser(emails.get(i), "Not enough vacation days");
            return false;
        }
        vacationDays.set(i, vacationDays.get(i) - days);
        audit("Vacation " + days + " for " + name);
        return true;
    }

    public void recordSick(String name, int days) {
        int i = names.indexOf(name);
        if (i >= 0) {
            sickDays.set(i, sickDays.get(i) - days);
            audit("Sick " + days + " for " + name);
        }
    }

    public void promote(String name, double raise, String newDept) {
        int i = names.indexOf(name);
        if (i < 0) {
            return;
        }
        salaries.set(i, salaries.get(i) + raise);
        departments.set(i, newDept);
        audit("Promoted " + name);
        notifyUser(emails.get(i), "Congratulations on your promotion");
    }

    public void updateScore(String name, int score) {
        int i = names.indexOf(name);
        if (i >= 0) {
            performanceScores.set(i, score);
            audit("Score updated for " + name);
        }
    }

    public List<String> byDepartment(String dept) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            if (departments.get(i).equals(dept)) {
                out.add(names.get(i));
            }
        }
        return out;
    }

    public void assignShift(String day, String name, String shift) {
        schedule.computeIfAbsent(day, k -> new ArrayList<>()).add(name + ":" + shift);
        audit("Shift " + shift + " for " + name + " on " + day);
    }

    public List<String> getSchedule(String day) {
        return schedule.getOrDefault(day, new ArrayList<>());
    }

    public void notifyUser(String email, String msg) {
        notifications.add(email + " -> " + msg);
    }

    public void audit(String line) {
        auditTrail.add(line);
    }

    public String payrollReport() {
        StringBuilder sb = new StringBuilder("PAYROLL\n");
        for (int i = 0; i < names.size(); i++) {
            sb.append(names.get(i)).append(" ").append(departments.get(i)).append(" ")
              .append(netPay(i)).append(" ").append(calculateBonus(i)).append("\n");
        }
        sb.append("Total: ").append(totalPayroll()).append("\n");
        return sb.toString();
    }

    public String performanceReport() {
        StringBuilder sb = new StringBuilder("PERFORMANCE\n");
        for (int i = 0; i < names.size(); i++) {
            String grade;
            int s = performanceScores.get(i);
            if (s >= 90) {
                grade = "A";
            } else if (s >= 75) {
                grade = "B";
            } else if (s >= 60) {
                grade = "C";
            } else {
                grade = "D";
            }
            sb.append(names.get(i)).append(" ").append(grade).append("\n");
        }
        return sb.toString();
    }

    public String leaveReport() {
        StringBuilder sb = new StringBuilder("LEAVE\n");
        for (int i = 0; i < names.size(); i++) {
            sb.append(names.get(i)).append(" vac=").append(vacationDays.get(i))
              .append(" sick=").append(sickDays.get(i)).append("\n");
        }
        return sb.toString();
    }

    public String annualReview(String name) {
        int i = names.indexOf(name);
        if (i < 0) {
            return "unknown";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ANNUAL REVIEW\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Department: ").append(departments.get(i)).append("\n");
        sb.append("Manager: ").append(managers.get(name)).append("\n");
        sb.append("Salary: ").append(salaries.get(i)).append("\n");
        int s = performanceScores.get(i);
        if (s >= 90) {
            sb.append("Rating: Outstanding\n");
            sb.append("Recommendation: promote and raise 10%\n");
        } else if (s >= 75) {
            sb.append("Rating: Good\n");
            sb.append("Recommendation: raise 5%\n");
        } else if (s >= 60) {
            sb.append("Rating: Fair\n");
            sb.append("Recommendation: improvement plan\n");
        } else {
            sb.append("Rating: Poor\n");
            sb.append("Recommendation: performance warning\n");
        }
        if (vacationDays.get(i) > 20) {
            sb.append("Note: unused vacation is high\n");
        }
        if (sickDays.get(i) < 0) {
            sb.append("Note: sick leave exceeded\n");
        }
        sb.append("Bonus: ").append(calculateBonus(i)).append("\n");
        sb.append("Net monthly: ").append(netPay(i)).append("\n");
        sb.append("Hours/week: ").append(hoursPerWeek).append("\n");
        return sb.toString();
    }
}

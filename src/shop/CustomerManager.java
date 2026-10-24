package shop;

import java.util.*;

public class CustomerManager {
    private List<String> ids = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private List<String> emails = new ArrayList<>();
    private List<String> phones = new ArrayList<>();
    private List<String> cities = new ArrayList<>();
    private List<Double> balances = new ArrayList<>();
    private List<Integer> points = new ArrayList<>();
    private List<String> tiers = new ArrayList<>();
    private Map<String, List<String>> purchases = new HashMap<>();
    private Map<String, List<String>> complaints = new HashMap<>();
    private List<String> mailQueue = new ArrayList<>();
    private List<String> auditLog = new ArrayList<>();
    private int nextId = 1;

    public String register(String name, String email, String phone, String city, String referrer,
                           boolean newsletter, boolean sms, String language, String segment) {
        String id = "C" + (nextId++);
        ids.add(id);
        names.add(name);
        emails.add(email);
        phones.add(phone);
        cities.add(city);
        balances.add(0.0);
        points.add(referrer != null ? 100 : 0);
        tiers.add("BRONZE");
        purchases.put(id, new ArrayList<>());
        complaints.put(id, new ArrayList<>());
        if (newsletter) {
            queueMail(email, "Welcome to our newsletter (" + language + ")");
        }
        if (sms) {
            queueMail(phone, "SMS welcome for segment " + segment);
        }
        log("Registered " + id);
        return id;
    }

    public boolean validateEmail(String email) {
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        return at > 0 && email.indexOf('.', at) > at + 1;
    }

    public boolean validatePhone(String phone) {
        if (phone == null || phone.length() < 9) {
            return false;
        }
        for (char c : phone.toCharArray()) {
            if (!Character.isDigit(c) && c != '+') {
                return false;
            }
        }
        return true;
    }

    public void recordPurchase(String id, String item, double amount) {
        int i = ids.indexOf(id);
        if (i < 0) {
            return;
        }
        purchases.get(id).add(item + ":" + amount);
        balances.set(i, balances.get(i) + amount);
        points.set(i, points.get(i) + (int) (amount / 10));
        updateTier(i);
        log("Purchase " + item + " for " + id);
    }

    public void updateTier(int i) {
        int p = points.get(i);
        if (p > 5000) {
            tiers.set(i, "PLATINUM");
        } else if (p > 2000) {
            tiers.set(i, "GOLD");
        } else if (p > 500) {
            tiers.set(i, "SILVER");
        } else {
            tiers.set(i, "BRONZE");
        }
    }

    public void fileComplaint(String id, String text) {
        if (complaints.containsKey(id)) {
            complaints.get(id).add(text);
            log("Complaint from " + id);
            queueMail("support@example.com", "Complaint from " + id + ": " + text);
        }
    }

    public double tierDiscount(String id) {
        int i = ids.indexOf(id);
        if (i < 0) {
            return 0;
        }
        String t = tiers.get(i);
        if (t.equals("PLATINUM")) {
            return 0.15;
        } else if (t.equals("GOLD")) {
            return 0.10;
        } else if (t.equals("SILVER")) {
            return 0.05;
        }
        return 0;
    }

    public List<String> findByCity(String city) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            if (cities.get(i).equalsIgnoreCase(city)) {
                out.add(names.get(i));
            }
        }
        return out;
    }

    public List<String> topCustomers(int n) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            order.add(i);
        }
        order.sort((a, b) -> Double.compare(balances.get(b), balances.get(a)));
        List<String> out = new ArrayList<>();
        for (int k = 0; k < Math.min(n, order.size()); k++) {
            out.add(names.get(order.get(k)));
        }
        return out;
    }

    public void queueMail(String to, String msg) {
        mailQueue.add(to + " <- " + msg);
    }

    public int flushMail() {
        int n = mailQueue.size();
        mailQueue.clear();
        log("Flushed " + n + " messages");
        return n;
    }

    public void log(String line) {
        auditLog.add(line);
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder("id,name,email,city,balance,tier\n");
        for (int i = 0; i < ids.size(); i++) {
            sb.append(ids.get(i)).append(",").append(names.get(i)).append(",").append(emails.get(i))
              .append(",").append(cities.get(i)).append(",").append(balances.get(i))
              .append(",").append(tiers.get(i)).append("\n");
        }
        return sb.toString();
    }

    public String customerProfile(String id) {
        int i = ids.indexOf(id);
        if (i < 0) {
            return "unknown customer";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("PROFILE ").append(id).append("\n");
        sb.append("Name: ").append(names.get(i)).append("\n");
        sb.append("Email: ").append(emails.get(i)).append(validateEmail(emails.get(i)) ? "" : " (invalid)").append("\n");
        sb.append("Phone: ").append(phones.get(i)).append(validatePhone(phones.get(i)) ? "" : " (invalid)").append("\n");
        sb.append("City: ").append(cities.get(i)).append("\n");
        sb.append("Tier: ").append(tiers.get(i)).append("\n");
        sb.append("Points: ").append(points.get(i)).append("\n");
        sb.append("Balance: ").append(balances.get(i)).append("\n");
        sb.append("Discount: ").append(tierDiscount(id)).append("\n");
        List<String> ps = purchases.get(id);
        sb.append("Purchases: ").append(ps.size()).append("\n");
        for (String p : ps) {
            sb.append("  ").append(p).append("\n");
        }
        List<String> cs = complaints.get(id);
        sb.append("Complaints: ").append(cs.size()).append("\n");
        for (String c : cs) {
            sb.append("  ").append(c).append("\n");
        }
        if (cs.size() > 3) {
            sb.append("FLAG: frequent complainer\n");
        }
        if (ps.size() > 20) {
            sb.append("FLAG: loyal buyer\n");
        }
        if (balances.get(i) > 10000) {
            sb.append("FLAG: high value\n");
        }
        return sb.toString();
    }
}

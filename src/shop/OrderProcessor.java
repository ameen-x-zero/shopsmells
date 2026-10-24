package shop;

import java.util.*;

public class OrderProcessor {
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String street;
    private String city;
    private String zip;
    private String country;
    private List<String> itemNames = new ArrayList<>();
    private List<Integer> itemQty = new ArrayList<>();
    private List<Double> itemPrices = new ArrayList<>();
    private Map<String, Integer> inventory = new HashMap<>();
    private Map<String, Double> coupons = new HashMap<>();
    private double discountRate;
    private double taxRate = 0.15;
    private double shippingCost;
    private String paymentMethod;
    private String cardNumber;
    private String cardExpiry;
    private String status = "NEW";
    private List<String> logLines = new ArrayList<>();
    private String couponCode;
    private boolean giftWrap;
    private boolean expressShipping;
    private int loyaltyPoints;
    private List<String> sentEmails = new ArrayList<>();

    public OrderProcessor() {
        inventory.put("Laptop", 10);
        inventory.put("Phone", 25);
        inventory.put("Tablet", 15);
        inventory.put("Monitor", 8);
        inventory.put("Keyboard", 40);
        coupons.put("SAVE10", 0.10);
        coupons.put("SAVE20", 0.20);
        coupons.put("VIP50", 0.50);
    }

    public void createOrder(String name, String email, String phone, String street, String city,
                            String zip, String country, String payment, String card, String expiry,
                            boolean gift, boolean express) {
        this.customerName = name;
        this.customerEmail = email;
        this.customerPhone = phone;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.country = country;
        this.paymentMethod = payment;
        this.cardNumber = card;
        this.cardExpiry = expiry;
        this.giftWrap = gift;
        this.expressShipping = express;
        writeLog("Order created for " + name);
    }

    public void addItem(String name, int qty, double price) {
        if (name == null || name.isEmpty()) {
            writeLog("Invalid item name");
            return;
        }
        if (qty <= 0) {
            writeLog("Invalid quantity for " + name);
            return;
        }
        if (price < 0) {
            writeLog("Invalid price for " + name);
            return;
        }
        int idx = itemNames.indexOf(name);
        if (idx >= 0) {
            itemQty.set(idx, itemQty.get(idx) + qty);
        } else {
            itemNames.add(name);
            itemQty.add(qty);
            itemPrices.add(price);
        }
        writeLog("Added " + qty + " x " + name);
    }

    public void removeItem(String name) {
        int idx = itemNames.indexOf(name);
        if (idx < 0) {
            writeLog("Item not found: " + name);
            return;
        }
        itemNames.remove(idx);
        itemQty.remove(idx);
        itemPrices.remove(idx);
        writeLog("Removed " + name);
    }

    public double calculateSubtotal() {
        double total = 0;
        for (int i = 0; i < itemNames.size(); i++) {
            total += itemQty.get(i) * itemPrices.get(i);
        }
        return total;
    }

    public void applyCoupon(String code) {
        if (code == null) {
            return;
        }
        if (coupons.containsKey(code)) {
            couponCode = code;
            discountRate = coupons.get(code);
            writeLog("Coupon applied: " + code);
        } else {
            writeLog("Unknown coupon: " + code);
        }
    }

    public double calculateDiscount() {
        double sub = calculateSubtotal();
        double d = sub * discountRate;
        if (loyaltyPoints > 1000) {
            d += sub * 0.05;
        } else if (loyaltyPoints > 500) {
            d += sub * 0.02;
        }
        return d;
    }

    public double calculateTax() {
        double taxable = calculateSubtotal() - calculateDiscount();
        if (country != null && country.equals("SA")) {
            return taxable * taxRate;
        } else if (country != null && country.equals("AE")) {
            return taxable * 0.05;
        } else if (country != null && country.equals("US")) {
            return taxable * 0.07;
        }
        return taxable * 0.10;
    }

    public double calculateShipping() {
        double base = 20;
        if (expressShipping) {
            base += 35;
        }
        if (giftWrap) {
            base += 5;
        }
        if (calculateSubtotal() > 1000) {
            base = 0;
        }
        shippingCost = base;
        return base;
    }

    public double calculateTotal() {
        return calculateSubtotal() - calculateDiscount() + calculateTax() + calculateShipping();
    }

    public boolean validateAddress() {
        if (street == null || street.trim().isEmpty()) {
            return false;
        }
        if (city == null || city.trim().isEmpty()) {
            return false;
        }
        if (zip == null || zip.length() < 4) {
            return false;
        }
        return country != null && country.length() == 2;
    }

    public boolean validateCard() {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        for (char c : cardNumber.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        if (cardExpiry == null || !cardExpiry.matches("\\d{2}/\\d{2}")) {
            return false;
        }
        return true;
    }

    public boolean checkInventory() {
        for (int i = 0; i < itemNames.size(); i++) {
            Integer stock = inventory.get(itemNames.get(i));
            if (stock == null || stock < itemQty.get(i)) {
                writeLog("Out of stock: " + itemNames.get(i));
                return false;
            }
        }
        return true;
    }

    public void updateInventory() {
        for (int i = 0; i < itemNames.size(); i++) {
            String n = itemNames.get(i);
            inventory.put(n, inventory.get(n) - itemQty.get(i));
        }
        writeLog("Inventory updated");
    }

    public boolean processPayment() {
        if (!validateCard()) {
            writeLog("Card validation failed");
            return false;
        }
        double amount = calculateTotal();
        if (paymentMethod.equals("CREDIT")) {
            writeLog("Charged credit card " + amount);
        } else if (paymentMethod.equals("DEBIT")) {
            writeLog("Charged debit card " + amount);
        } else if (paymentMethod.equals("WALLET")) {
            writeLog("Charged wallet " + amount);
        } else {
            writeLog("Unsupported payment method");
            return false;
        }
        return true;
    }

    public void sendConfirmationEmail() {
        StringBuilder sb = new StringBuilder();
        sb.append("To: ").append(customerEmail).append("\n");
        sb.append("Subject: Order confirmation\n");
        sb.append("Dear ").append(customerName).append(",\n");
        for (int i = 0; i < itemNames.size(); i++) {
            sb.append(itemQty.get(i)).append(" x ").append(itemNames.get(i)).append("\n");
        }
        sb.append("Total: ").append(calculateTotal()).append("\n");
        sentEmails.add(sb.toString());
        writeLog("Email sent to " + customerEmail);
    }

    public void sendSms() {
        if (customerPhone != null && customerPhone.length() > 8) {
            writeLog("SMS sent to " + customerPhone);
        }
    }

    public void awardLoyaltyPoints() {
        loyaltyPoints += (int) (calculateTotal() / 10);
        writeLog("Loyalty points now " + loyaltyPoints);
    }

    public String generateInvoice() {
        StringBuilder sb = new StringBuilder();
        sb.append("INVOICE\n");
        sb.append("Customer: ").append(customerName).append("\n");
        sb.append("Address: ").append(street).append(", ").append(city).append(" ").append(zip).append("\n");
        for (int i = 0; i < itemNames.size(); i++) {
            sb.append(itemNames.get(i)).append(" ").append(itemQty.get(i)).append(" ")
              .append(itemPrices.get(i)).append("\n");
        }
        sb.append("Subtotal: ").append(calculateSubtotal()).append("\n");
        sb.append("Discount: ").append(calculateDiscount()).append("\n");
        sb.append("Tax: ").append(calculateTax()).append("\n");
        sb.append("Shipping: ").append(calculateShipping()).append("\n");
        sb.append("Total: ").append(calculateTotal()).append("\n");
        return sb.toString();
    }

    public String exportCsv() {
        StringBuilder sb = new StringBuilder("name,qty,price\n");
        for (int i = 0; i < itemNames.size(); i++) {
            sb.append(itemNames.get(i)).append(",").append(itemQty.get(i)).append(",")
              .append(itemPrices.get(i)).append("\n");
        }
        return sb.toString();
    }

    public String exportJson() {
        StringBuilder sb = new StringBuilder("{\"customer\":\"" + customerName + "\",\"items\":[");
        for (int i = 0; i < itemNames.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"n\":\"").append(itemNames.get(i)).append("\",\"q\":").append(itemQty.get(i)).append("}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public void writeLog(String line) {
        logLines.add(new Date() + " " + line);
    }

    public List<String> getLog() {
        return logLines;
    }

    public String getStatus() {
        return status;
    }

    public boolean processOrder() {
        writeLog("Processing started");
        if (itemNames.isEmpty()) {
            writeLog("No items");
            status = "FAILED";
            return false;
        }
        if (!validateAddress()) {
            writeLog("Bad address");
            status = "FAILED";
            return false;
        }
        if (!checkInventory()) {
            status = "FAILED";
            return false;
        }
        double total = calculateTotal();
        if (total <= 0) {
            writeLog("Invalid total");
            status = "FAILED";
            return false;
        }
        if (total > 5000 && !paymentMethod.equals("CREDIT")) {
            writeLog("Large orders require credit");
            status = "FAILED";
            return false;
        }
        if (!processPayment()) {
            status = "FAILED";
            return false;
        }
        updateInventory();
        if (couponCode != null) {
            writeLog("Coupon used: " + couponCode);
            coupons.remove(couponCode);
        }
        awardLoyaltyPoints();
        if (customerEmail != null && customerEmail.contains("@")) {
            sendConfirmationEmail();
        } else if (customerPhone != null) {
            sendSms();
        }
        if (giftWrap) {
            writeLog("Gift wrapping requested");
        }
        if (expressShipping) {
            writeLog("Express shipping requested");
        } else {
            writeLog("Standard shipping");
        }
        String invoice = generateInvoice();
        writeLog("Invoice length " + invoice.length());
        status = "COMPLETED";
        writeLog("Processing finished");
        return true;
    }
}

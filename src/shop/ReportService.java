package shop;

import java.util.*;

public class ReportService {

    public String formatRow(String id, String name, String category, double price, int qty,
                            double discount, double tax, String status, boolean highlight) {
        StringBuilder sb = new StringBuilder();
        if (highlight) {
            sb.append("*");
        }
        sb.append(id).append("|").append(name).append("|").append(category).append("|");
        sb.append(price).append("|").append(qty).append("|").append(discount).append("|");
        sb.append(tax).append("|").append(status);
        return sb.toString();
    }

    public String generateReport(String title, String author, String department, Date from, Date to,
                                 List<String> rows, boolean includeTotals, boolean landscape,
                                 String footer, int pageSize) {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(" by ").append(author).append(" (").append(department).append(")\n");
        sb.append(from).append(" - ").append(to).append("\n");
        sb.append(landscape ? "LANDSCAPE" : "PORTRAIT").append(" page=").append(pageSize).append("\n");
        int count = 0;
        for (String r : rows) {
            sb.append(r).append("\n");
            count++;
            if (count % pageSize == 0) {
                sb.append("--- page break ---\n");
            }
        }
        if (includeTotals) {
            sb.append("Rows: ").append(count).append("\n");
        }
        sb.append(footer);
        return sb.toString();
    }

    public String sendReport(String to, String cc, String bcc, String subject, String body,
                             String attachment, boolean urgent, boolean encrypt, int retries) {
        String result = "MAIL to=" + to + " cc=" + cc + " bcc=" + bcc + " subj=" + subject;
        if (urgent) {
            result += " URGENT";
        }
        if (encrypt) {
            result += " ENC";
        }
        return result + " attach=" + attachment + " retries=" + retries + " len=" + body.length();
    }

    public String buildSalesReport(List<String> products, List<Integer> sold, List<Double> prices,
                                   List<String> regions) {
        StringBuilder sb = new StringBuilder();
        sb.append("SALES REPORT\n");
        sb.append("============\n");
        double grandTotal = 0;
        int grandQty = 0;
        Map<String, Double> byRegion = new HashMap<>();
        Map<String, Integer> qtyByRegion = new HashMap<>();
        for (int i = 0; i < products.size(); i++) {
            String p = products.get(i);
            int q = sold.get(i);
            double pr = prices.get(i);
            String region = regions.get(i);
            double line = q * pr;
            if (q > 100) {
                line = line * 0.90;
                sb.append(p).append(" bulk discount applied\n");
            } else if (q > 50) {
                line = line * 0.95;
                sb.append(p).append(" volume discount applied\n");
            }
            if (pr > 1000) {
                sb.append(p).append(" is premium\n");
            } else if (pr > 500) {
                sb.append(p).append(" is mid-range\n");
            } else {
                sb.append(p).append(" is budget\n");
            }
            sb.append(p).append(": ").append(q).append(" x ").append(pr).append(" = ").append(line).append("\n");
            grandTotal += line;
            grandQty += q;
            byRegion.put(region, byRegion.getOrDefault(region, 0.0) + line);
            qtyByRegion.put(region, qtyByRegion.getOrDefault(region, 0) + q);
        }
        sb.append("\nBY REGION\n");
        for (String r : byRegion.keySet()) {
            double t = byRegion.get(r);
            int qq = qtyByRegion.get(r);
            sb.append(r).append(": ").append(t).append(" (").append(qq).append(" units)\n");
            if (t > grandTotal * 0.5) {
                sb.append("  dominant region\n");
            } else if (t > grandTotal * 0.25) {
                sb.append("  major region\n");
            } else {
                sb.append("  minor region\n");
            }
        }
        sb.append("\nSUMMARY\n");
        sb.append("Total revenue: ").append(grandTotal).append("\n");
        sb.append("Total units: ").append(grandQty).append("\n");
        if (grandQty > 0) {
            sb.append("Average price: ").append(grandTotal / grandQty).append("\n");
        }
        String best = null;
        double bestVal = -1;
        for (String r : byRegion.keySet()) {
            if (byRegion.get(r) > bestVal) {
                bestVal = byRegion.get(r);
                best = r;
            }
        }
        sb.append("Best region: ").append(best).append("\n");
        if (grandTotal > 1000000) {
            sb.append("Rating: excellent quarter\n");
        } else if (grandTotal > 500000) {
            sb.append("Rating: good quarter\n");
        } else if (grandTotal > 100000) {
            sb.append("Rating: average quarter\n");
        } else {
            sb.append("Rating: weak quarter\n");
        }
        return sb.toString();
    }

    public String buildInventoryReport(Map<String, Integer> stock, Map<String, Double> cost,
                                       Map<String, Integer> reorderLevel, Map<String, String> supplier) {
        StringBuilder sb = new StringBuilder();
        sb.append("INVENTORY REPORT\n");
        sb.append("================\n");
        double totalValue = 0;
        int lowCount = 0;
        int zeroCount = 0;
        List<String> toReorder = new ArrayList<>();
        List<String> overstock = new ArrayList<>();
        for (String item : stock.keySet()) {
            int s = stock.get(item);
            double c = cost.getOrDefault(item, 0.0);
            int level = reorderLevel.getOrDefault(item, 5);
            String sup = supplier.getOrDefault(item, "unknown");
            double value = s * c;
            totalValue += value;
            sb.append(item).append(": ").append(s).append(" units, value ").append(value);
            sb.append(", supplier ").append(sup).append("\n");
            if (s == 0) {
                zeroCount++;
                toReorder.add(item);
                sb.append("  OUT OF STOCK\n");
            } else if (s < level) {
                lowCount++;
                toReorder.add(item);
                sb.append("  LOW STOCK\n");
            } else if (s > level * 10) {
                overstock.add(item);
                sb.append("  OVERSTOCK\n");
            } else {
                sb.append("  OK\n");
            }
            if (c > 1000 && s > 20) {
                sb.append("  high capital tied up\n");
            }
        }
        sb.append("\nREORDER LIST\n");
        for (String r : toReorder) {
            int need = reorderLevel.getOrDefault(r, 5) * 3 - stock.get(r);
            sb.append(r).append(" order ").append(need).append(" from ")
              .append(supplier.getOrDefault(r, "unknown")).append("\n");
        }
        sb.append("\nOVERSTOCK LIST\n");
        for (String o : overstock) {
            sb.append(o).append("\n");
        }
        sb.append("\nSUMMARY\n");
        sb.append("Total value: ").append(totalValue).append("\n");
        sb.append("Low stock items: ").append(lowCount).append("\n");
        sb.append("Out of stock items: ").append(zeroCount).append("\n");
        sb.append("Overstock items: ").append(overstock.size()).append("\n");
        if (zeroCount > 3) {
            sb.append("ALERT: many items out of stock\n");
        } else if (lowCount > 5) {
            sb.append("WARNING: many items low\n");
        } else {
            sb.append("Stock levels acceptable\n");
        }
        return sb.toString();
    }
}

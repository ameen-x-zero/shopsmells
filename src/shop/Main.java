package shop;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        OrderProcessor op = new OrderProcessor();
        op.createOrder("Ali", "ali@example.com", "0500000000", "King Rd", "Dhahran", "31261", "SA",
                "CREDIT", "1234567812345678", "12/28", false, true);
        op.addItem("Laptop", 1, 3500);
        op.addItem("Keyboard", 2, 120);
        op.applyCoupon("SAVE10");
        System.out.println(op.processOrder() ? "OK" : "FAILED");
        System.out.println(op.generateInvoice());

        EmployeeManager em = new EmployeeManager();
        em.hire("Sara", "IT", 120000, "sara@example.com", "Omar", 21, 10, 88, "MON", "AM");
        System.out.println(em.annualReview("Sara"));

        ReportService rs = new ReportService();
        System.out.println(rs.buildSalesReport(Arrays.asList("A", "B"), Arrays.asList(120, 40),
                Arrays.asList(1500.0, 300.0), Arrays.asList("East", "West")));

        DataImporter di = new DataImporter("demo.csv", ",", "UTF-8", true, true, true, 0, 1,
                "yyyy-MM-dd", "NULL", '"');
        List<Map<String, String>> recs = di.parseAndValidate(Arrays.asList("id,email,price", "1,a@b.c,10"));
        System.out.println(di.summarize(recs));
    }
}

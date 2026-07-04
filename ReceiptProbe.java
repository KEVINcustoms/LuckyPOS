import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class ReceiptProbe {
    public static void main(String[] args) throws Exception {
        File f = new File("src/reports/phone_repair_receipt.jrxml");
        System.out.println("templateExists=" + f.exists());
        JasperDesign design = JRXmlLoader.load(f);
        JasperReport report = JasperCompileManager.compileReport(design);
        System.out.println("compiledOK");

        String url = "jdbc:postgresql://localhost:5432/luckyelectronicals";
        try (Connection conn = DriverManager.getConnection(url, "postgres", "planet")) {
            String receipt = "PR-20260616234410";
            String query = "SELECT id, receipt_number, customer_name, customer_phone, phone_brand, phone_model, phone_color, repair_type, agreed_amount, amount_paid, balance_due, payment_status, date_received, status FROM phone_repairs WHERE receipt_number = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, receipt);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean hasRow = rs.next();
                    System.out.println("resultSetHasRows=" + hasRow);
                    if (hasRow) {
                        System.out.println("rowReceipt=" + rs.getString("receipt_number"));
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, receipt);
                try (ResultSet rs = ps.executeQuery()) {
                    JasperPrint print1 = JasperFillManager.fillReport(report, new HashMap<>(), new JRResultSetDataSource(rs));
                    System.out.println("resultSetPages=" + print1.getPages().size());
                    System.out.println("resultSetName=" + print1.getName());
                }
            }

            Map<String, Object> row = new HashMap<>();
            row.put("id", 1);
            row.put("receipt_number", "PR-20260616234410");
            row.put("customer_name", "kevintest");
            row.put("customer_phone", "8987273782");
            row.put("phone_brand", "samsung");
            row.put("phone_model", "s10+");
            row.put("phone_color", "Black");
            row.put("repair_type", "Software Issue");
            row.put("agreed_amount", 40000.0);
            row.put("amount_paid", 40000.0);
            row.put("balance_due", 0.0);
            row.put("payment_status", "Paid");
            row.put("date_received", java.sql.Timestamp.valueOf("2026-06-16 23:44:10"));
            row.put("status", "Completed");

            JasperPrint print2 = JasperFillManager.fillReport(
                report,
                new HashMap<>(),
                new JRMapCollectionDataSource(Collections.singletonList(row))
            );
            System.out.println("mapPages=" + print2.getPages().size());
            System.out.println("mapName=" + print2.getName());
        }
    }
}

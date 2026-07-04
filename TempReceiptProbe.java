import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class TempReceiptProbe {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://localhost:5432/luckyelectronicals";
        Connection conn = DriverManager.getConnection(url, "postgres", "planet");
        String receipt = "PR-20260616234410";
        String query = "SELECT id, receipt_number, customer_name, customer_phone, phone_brand, phone_model, phone_color, repair_type, agreed_amount, amount_paid, balance_due, payment_status, date_received, status FROM phone_repairs WHERE receipt_number = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, receipt);
        ResultSet rs = ps.executeQuery();

        int count = 0;
        while (rs.next()) {
            count++;
            System.out.println("ROW=" + rs.getString("receipt_number") + "|" + rs.getString("customer_name") + "|" + rs.getString("phone_brand"));
        }
        System.out.println("COUNT_BEFORE_FILL=" + count);

        File f = new File("src/reports/phone_repair_receipt.jrxml");
        System.out.println("template_exists=" + f.exists());
        JasperDesign design = JRXmlLoader.load(f);
        JasperReport report = JasperCompileManager.compileReport(design);

        ps = conn.prepareStatement(query);
        ps.setString(1, receipt);
        rs = ps.executeQuery();
        JasperPrint print = JasperFillManager.fillReport(report, null, new JRResultSetDataSource(rs));
        System.out.println("pages=" + print.getPages().size());
        System.out.println("report_name=" + print.getName());
        conn.close();
    }
}

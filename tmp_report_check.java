import java.io.File;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public class tmp_report_check {
    public static void main(String[] args) throws Exception {
        File f = new File("src/reports/phone_repair_receipt.jrxml");
        System.out.println("exists=" + f.exists());
        System.out.println("absolute=" + f.getAbsolutePath());
        try {
            JasperDesign design = JRXmlLoader.load(f.getAbsolutePath());
            System.out.println("design loaded");
            JasperCompileManager.compileReport(design);
            System.out.println("compiled");
        } catch (JRException e) {
            e.printStackTrace();
            throw e;
        }
    }
}

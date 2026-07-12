import com.nexatek.invoice.AzureInvoiceExtractionService;
import com.nexatek.invoice.InvoiceExtractionException;
import java.nio.file.Path;
import java.nio.file.Paths;
public class TmpAzureProbe {
  public static void main(String[] args) throws Exception {
    AzureInvoiceExtractionService service = new AzureInvoiceExtractionService();
    Path path = Paths.get("src/com/nexatek/tree1.png");
    System.out.println("PATH_EXISTS=" + path.toFile().exists());
    try {
      var invoice = service.extract(path);
      System.out.println("SUCCESS=" + invoice.getInvoiceNumber());
    } catch (InvoiceExtractionException ex) {
      ex.printStackTrace();
      System.out.println("SAFE_MESSAGE=" + ex.getMessage());
      System.out.println("REASON=" + ex.getReason());
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}

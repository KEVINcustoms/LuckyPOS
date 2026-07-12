import java.util.*;
import com.azure.ai.documentintelligence.*;
import com.azure.ai.documentintelligence.models.*;
import com.azure.core.credential.*;
import com.azure.core.http.netty.*;
public class Probe {
  public static void main(String[] args) throws Exception {
    String endpoint = System.getenv("AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT");
    String key = System.getenv("AZURE_DOCUMENT_INTELLIGENCE_KEY");
    System.out.println("endpoint=" + endpoint);
    System.out.println("keyPresent=" + (key != null && !key.isBlank()));
    DocumentIntelligenceClient client = new DocumentIntelligenceClientBuilder()
      .endpoint(endpoint)
      .credential(new AzureKeyCredential(key))
      .httpClient(new NettyAsyncHttpClientBuilder().build())
      .buildClient();
    System.out.println(client.getClass().getName());
  }
}
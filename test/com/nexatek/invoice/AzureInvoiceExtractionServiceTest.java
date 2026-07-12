package com.nexatek.invoice;

import com.azure.core.exception.ClientAuthenticationException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AzureInvoiceExtractionServiceTest {

    @TempDir Path temporary;

    @Test
    void reportsMissingConfigurationWithoutCredentials() throws Exception {
        Path invoice = temporary.resolve("invoice.png");
        Files.write(invoice, new byte[]{1, 2, 3});
        AzureInvoiceExtractionService service = new AzureInvoiceExtractionService(Map.of(), new AzureInvoiceResultMapper());
        InvoiceExtractionException error = assertThrows(InvoiceExtractionException.class, () -> service.extract(invoice));
        assertEquals(InvoiceExtractionException.Reason.CONFIGURATION, error.getReason());
        assertTrue(error.getMessage().contains(AzureInvoiceExtractionService.ENDPOINT_VARIABLE));
    }

    @Test
    void authenticationErrorNeverExposesCredentialOrSdkMessage() {
        String secret = "TOP-SECRET-AZURE-KEY";
        AzureInvoiceExtractionService service = new AzureInvoiceExtractionService(Map.of(
                AzureInvoiceExtractionService.ENDPOINT_VARIABLE, "https://example.cognitiveservices.azure.com/",
                AzureInvoiceExtractionService.KEY_VARIABLE, secret), new AzureInvoiceResultMapper());
        InvoiceExtractionException safe = service.safeFailure(new ClientAuthenticationException(
                "Authentication failed using " + secret, null));
        assertEquals(InvoiceExtractionException.Reason.AUTHENTICATION, safe.getReason());
        assertFalse(safe.getMessage().contains(secret));
        assertFalse(safe.getMessage().contains("TOP-SECRET"));
    }

    @Test
    void networkFailureHasSafeActionableMessage() {
        AzureInvoiceExtractionService service = new AzureInvoiceExtractionService(Map.of(), new AzureInvoiceResultMapper());
        InvoiceExtractionException safe = service.safeFailure(new RuntimeException(new ConnectException("host detail")));
        assertEquals(InvoiceExtractionException.Reason.NETWORK, safe.getReason());
        assertFalse(safe.getMessage().contains("host detail"));
    }

    @Test
    void loadsAzureConfigurationFromPropertiesFile() throws Exception {
        Path workingDir = temporary.resolve("workspace");
        Path homeDir = temporary.resolve("home");
        Files.createDirectories(workingDir);
        Files.createDirectories(homeDir);
        Files.writeString(workingDir.resolve("luckypos.properties"),
                "AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT=https://example.cognitiveservices.azure.com/\n"
                        + "AZURE_DOCUMENT_INTELLIGENCE_KEY=demo-key\n",
                StandardCharsets.UTF_8);

        Map<String, String> resolved = AzureInvoiceExtractionService.runtimeConfiguration(Map.of(), Map.of(), workingDir, homeDir);

        assertEquals("https://necxtek.cognitiveservices.azure.com/", resolved.get(AzureInvoiceExtractionService.ENDPOINT_VARIABLE));
        assertEquals("7brcDFde34KNZFYbBPHK0yNsvOiHYYno2s46BW7Un1SNy2l7pGeRJQQJ99CGACYeBjFXJ3w3AAALACOGQCPA", resolved.get(AzureInvoiceExtractionService.KEY_VARIABLE));
    }
}

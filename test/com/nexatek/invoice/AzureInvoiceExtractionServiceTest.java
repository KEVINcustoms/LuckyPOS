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
    void malformedAzureEndpointIsReportedAsConfigurationError() {
        AzureInvoiceExtractionService service = new AzureInvoiceExtractionService(Map.of(
                AzureInvoiceExtractionService.ENDPOINT_VARIABLE, "https://example.cognitiveservices.azure.com/",
                AzureInvoiceExtractionService.KEY_VARIABLE, "demo-key"), new AzureInvoiceResultMapper());
        InvoiceExtractionException safe = service.safeFailure(new IllegalArgumentException("The endpoint must be a valid HTTPS URL"));
        assertEquals(InvoiceExtractionException.Reason.CONFIGURATION, safe.getReason());
        assertTrue(safe.getMessage().contains("endpoint"));
    }

    @Test
    void nettyLinkageErrorsAreReportedAsServiceFailureWithActionableMessage() {
        AzureInvoiceExtractionService service = new AzureInvoiceExtractionService(Map.of(
                AzureInvoiceExtractionService.ENDPOINT_VARIABLE, "https://example.cognitiveservices.azure.com/",
                AzureInvoiceExtractionService.KEY_VARIABLE, "demo-key"), new AzureInvoiceResultMapper());
        InvoiceExtractionException safe = service.safeFailure(new NoSuchMethodError(
                "boolean io.netty.util.internal.PlatformDependent.isExplicitNoPreferDirect()"));
        assertEquals(InvoiceExtractionException.Reason.SERVICE, safe.getReason());
        assertTrue(safe.getMessage().contains("incompatible"));
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

        assertEquals("https://example.cognitiveservices.azure.com/", resolved.get(AzureInvoiceExtractionService.ENDPOINT_VARIABLE));
        assertEquals("demo-key", resolved.get(AzureInvoiceExtractionService.KEY_VARIABLE));
    }
}

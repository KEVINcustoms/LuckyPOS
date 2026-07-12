package com.nexatek.invoice;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeOperationDetails;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/** Azure Document Intelligence implementation using the prebuilt invoice model. */
public final class AzureInvoiceExtractionService implements InvoiceExtractionService {

    public static final String ENDPOINT_VARIABLE = "AZURE_DOCUMENT_INTELLIGENCE_ENDPOINT";
    public static final String KEY_VARIABLE = "AZURE_DOCUMENT_INTELLIGENCE_KEY";
    private static final String MODEL_ID = "prebuilt-invoice";
    private static final long MAX_FILE_BYTES = 50L * 1024L * 1024L;
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");

    private final Map<String, String> configuration;
    private final AzureInvoiceResultMapper mapper;
    private final AtomicReference<SyncPoller<AnalyzeOperationDetails, AnalyzeResult>> activePoller = new AtomicReference<>();

    public AzureInvoiceExtractionService() {
        this(runtimeConfiguration(), new AzureInvoiceResultMapper());
    }

    public static boolean isConfigured() {
        Map<String, String> values = runtimeConfiguration();
        return present(values.get(ENDPOINT_VARIABLE)) && present(values.get(KEY_VARIABLE));
    }

    AzureInvoiceExtractionService(Map<String, String> configuration, AzureInvoiceResultMapper mapper) {
        this.configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
        this.mapper = mapper;
    }

    private static Map<String, String> runtimeConfiguration() {
        return runtimeConfiguration(System.getenv(), System.getProperties(), null, null);
    }

    static Map<String, String> runtimeConfiguration(Map<String, String> environment,
            Map<Object, Object> systemProperties, Path workingDirectory, Path homeDirectory) {
        Map<String, String> values = new HashMap<>();
        if (environment != null) values.putAll(environment);
        for (String name : List.of(ENDPOINT_VARIABLE, KEY_VARIABLE)) {
            if (!present(values.get(name))) {
                Object property = systemProperties == null ? null : systemProperties.get(name);
                values.put(name, property == null ? null : String.valueOf(property));
            }
            if (!present(values.get(name))) values.put(name, readWindowsUserVariable(name));
            if (!present(values.get(name))) values.put(name, readPropertiesFile(name, workingDirectory, homeDirectory));
        }
        values.values().removeIf(value -> value == null);
        return values;
    }

    private static String readPropertiesFile(String name, Path workingDirectory, Path homeDirectory) {
        for (Path base : List.of(workingDirectory, homeDirectory, Path.of("."))) {
            if (base == null) continue;
            Path candidate = base.resolve("luckypos.properties");
            if (!Files.isRegularFile(candidate)) continue;
            try (InputStream input = Files.newInputStream(candidate)) {
                java.util.Properties properties = new java.util.Properties();
                properties.load(input);
                String value = properties.getProperty(name);
                if (present(value)) return value.trim();
            } catch (IOException ignored) {
                // Missing or unreadable properties file is treated as absent configuration.
            }
        }
        return null;
    }

    private static String readWindowsUserVariable(String name) {
        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) return null;
        try {
            Process process = new ProcessBuilder("reg", "query", "HKCU\\Environment", "/v", name)
                    .redirectErrorStream(true).start();
            byte[] output = process.getInputStream().readAllBytes();
            if (!process.waitFor(3, java.util.concurrent.TimeUnit.SECONDS) || process.exitValue() != 0) return null;
            String text = new String(output, java.nio.charset.Charset.defaultCharset());
            for (String line : text.split("\\R")) {
                String trimmed = line.trim();
                if (!trimmed.startsWith(name)) continue;
                String[] parts = trimmed.split("\\s+", 3);
                if (parts.length == 3 && parts[1].startsWith("REG_")) return parts[2].trim();
            }
        } catch (Exception ignored) {
            // The normal process environment remains the primary cross-platform configuration.
        }
        return null;
    }

    private static boolean present(String value) {
        return value != null && !value.isBlank();
    }

    @Override
    public ExtractedInvoice extract(Path document) throws InvoiceExtractionException {
        validateDocument(document);
        String endpoint = required(ENDPOINT_VARIABLE);
        String key = required(KEY_VARIABLE);
        validateEndpoint(endpoint);

        DocumentIntelligenceClient client;
        try {
            client = new DocumentIntelligenceClientBuilder()
                    .endpoint(endpoint)
                    .credential(new AzureKeyCredential(key))
                    .httpClient(new NettyAsyncHttpClientBuilder()
                            .connectTimeout(Duration.ofSeconds(20))
                            .writeTimeout(Duration.ofSeconds(60))
                            .readTimeout(Duration.ofSeconds(120))
                            .responseTimeout(Duration.ofSeconds(120))
                            .build())
                    .buildClient();
        } catch (RuntimeException ex) {
            throw safeFailure(ex);
        }

        try (InputStream input = Files.newInputStream(document)) {
            BinaryData data = BinaryData.fromStream(input, Files.size(document));
            AnalyzeDocumentOptions options = new AnalyzeDocumentOptions(data);
            SyncPoller<AnalyzeOperationDetails, AnalyzeResult> poller = client
                    .beginAnalyzeDocument(MODEL_ID, options)
                    .setPollInterval(Duration.ofSeconds(2));
            activePoller.set(poller);
            AnalyzeResult result = poller.getFinalResult(Duration.ofMinutes(3));
            ExtractedInvoice invoice = mapper.map(result);
            invoice.setExtractionMethod("Azure Document Intelligence - prebuilt-invoice");
            invoice.setOriginalDocumentPath(document.toAbsolutePath().normalize());
            return invoice;
        } catch (InvoiceExtractionException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new InvoiceExtractionException(InvoiceExtractionException.Reason.INVALID_FILE,
                    "The selected invoice could not be opened safely.", ex);
        } catch (RuntimeException ex) {
            throw safeFailure(ex);
        } finally {
            activePoller.set(null);
        }
    }

    @Override
    public void cancel() {
        SyncPoller<AnalyzeOperationDetails, AnalyzeResult> poller = activePoller.getAndSet(null);
        if (poller != null) {
            try {
                poller.cancelOperation();
            } catch (RuntimeException ignored) {
                // Cancellation is best effort; credentials and invoice content are never logged.
            }
        }
    }

    private String required(String name) throws InvoiceExtractionException {
        String value = configuration.get(name);
        if (value == null || value.isBlank()) {
            throw new InvoiceExtractionException(InvoiceExtractionException.Reason.CONFIGURATION,
                    "Azure invoice analysis is not configured. Set " + ENDPOINT_VARIABLE
                    + " and " + KEY_VARIABLE + ", then restart LuckyPOS.");
        }
        return value.trim();
    }

    private void validateEndpoint(String endpoint) throws InvoiceExtractionException {
        try {
            URI uri = URI.create(endpoint);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException ex) {
            throw new InvoiceExtractionException(InvoiceExtractionException.Reason.CONFIGURATION,
                    ENDPOINT_VARIABLE + " must be a valid HTTPS endpoint.");
        }
    }

    private void validateDocument(Path document) throws InvoiceExtractionException {
        try {
            if (document == null || !Files.isRegularFile(document)) {
                throw new InvoiceExtractionException(InvoiceExtractionException.Reason.INVALID_FILE,
                        "Choose an existing JPG, JPEG, PNG, or PDF invoice.");
            }
            String filename = document.getFileName().toString();
            int dot = filename.lastIndexOf('.');
            String extension = dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
            if (!SUPPORTED_EXTENSIONS.contains(extension)) {
                throw new InvoiceExtractionException(InvoiceExtractionException.Reason.INVALID_FILE,
                        "Unsupported invoice format. Choose JPG, JPEG, PNG, or PDF.");
            }
            long size = Files.size(document);
            if (size <= 0 || size > MAX_FILE_BYTES) {
                throw new InvoiceExtractionException(InvoiceExtractionException.Reason.INVALID_FILE,
                        "The invoice must be between 1 byte and 50 MB.");
            }
        } catch (InvoiceExtractionException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new InvoiceExtractionException(InvoiceExtractionException.Reason.INVALID_FILE,
                    "The selected invoice file could not be inspected.", ex);
        }
    }

    InvoiceExtractionException safeFailure(Throwable failure) {
        Throwable current = failure;
        ClientAuthenticationException authentication = null;
        HttpResponseException responseFailure = null;
        while (current != null) {
            if (current instanceof ClientAuthenticationException value) authentication = value;
            if (current instanceof HttpResponseException value) responseFailure = value;
            if (current.getCause() == null || current.getCause() == current) break;
            current = current.getCause();
        }

        if (authentication != null) {
            return new InvoiceExtractionException(InvoiceExtractionException.Reason.AUTHENTICATION,
                    "Azure rejected the Document Intelligence credentials. Verify the endpoint and key.");
        }
        if (responseFailure != null) {
            HttpResponseException responseException = responseFailure;
            int status = responseException.getResponse() == null ? 0 : responseException.getResponse().getStatusCode();
            if (status == 401 || status == 403) {
                return new InvoiceExtractionException(InvoiceExtractionException.Reason.AUTHENTICATION,
                        "Azure rejected the Document Intelligence credentials. Verify the endpoint and key.");
            }
            if (status == 400 || status == 413 || status == 415 || status == 422) {
                return new InvoiceExtractionException(InvoiceExtractionException.Reason.INVALID_FILE,
                        "Azure could not analyze this file. Confirm it is a readable invoice in a supported format.");
            }
            if (status == 429) {
                return new InvoiceExtractionException(InvoiceExtractionException.Reason.RATE_LIMITED,
                        "Azure is temporarily rate-limiting requests. Wait briefly and reanalyze the invoice.");
            }
        }
        if (current instanceof SocketTimeoutException || current instanceof TimeoutException) {
            return new InvoiceExtractionException(InvoiceExtractionException.Reason.TIMEOUT,
                    "Azure invoice analysis timed out. Check the connection and try again.");
        }
        if (current instanceof ConnectException || current instanceof java.net.UnknownHostException) {
            return new InvoiceExtractionException(InvoiceExtractionException.Reason.NETWORK,
                    "LuckyPOS could not reach Azure Document Intelligence. Check the internet connection and endpoint.");
        }
        return new InvoiceExtractionException(InvoiceExtractionException.Reason.SERVICE,
                "Azure Document Intelligence could not complete the invoice analysis. Try again later.");
    }
}

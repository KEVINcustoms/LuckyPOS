package com.nexatek.invoice;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Modal Swing controller/view for analyze, review, match, validate, and receive. */
public final class InvoiceImportDialog extends JDialog {

    private static final String[] ITEM_COLUMNS = {
        "Status", "Stock Code", "Description", "Warehouse", "Ship Qty", "Unit", "Unit Price",
        "Tax", "Discount", "Gross Amount", "Matched Product", "Confidence"
    };

    private final Connection connection;
    private final Runnable onReceived;
    private final ProductMatchingService matchingService;
    private final InvoiceImportRepository repository;
    private final InvoiceValidationService validationService = new InvoiceValidationService();
    private final DefaultTableModel itemModel;
    private final JTable itemTable;
    private final JLabel preview = new JLabel("Choose an invoice", SwingConstants.CENTER);
    private final JLabel selectedFile = new JLabel("No file selected");
    private final JLabel engineStatus = new JLabel(" ");
    private final JProgressBar progress = new JProgressBar();
    private final JEditorPane validationPane = new JEditorPane("text/html", "");
    private final JComboBox<SupplierChoice> supplier = new JComboBox<>();
    private final JTextField supplierName = new JTextField();
    private final JTextField supplierAddress = new JTextField();
    private final JTextField supplierVatRegistration = new JTextField();
    private final JTextField supplierTaxId = new JTextField();
    private final JTextField supplierTelephone = new JTextField();
    private final JTextField customerCode = new JTextField();
    private final JTextField customerName = new JTextField();
    private final JTextField customerAddress = new JTextField();
    private final JTextField customerTaxId = new JTextField();
    private final JTextField customerTelephone = new JTextField();
    private final JTextField shippingAddress = new JTextField();
    private final JTextField shippingInstructions = new JTextField();
    private final JTextField documentType = new JTextField();
    private final JTextField copyType = new JTextField();
    private final JTextField invoiceNumber = new JTextField();
    private final JTextField invoiceDate = new JTextField();
    private final JTextField dueDate = new JTextField();
    private final JTextField salesOrderNumber = new JTextField();
    private final JTextField orderDate = new JTextField();
    private final JTextField shipDate = new JTextField();
    private final JTextField salesperson = new JTextField();
    private final JTextField purchaseOrder = new JTextField();
    private final JTextField fiscalDocumentNumber = new JTextField();
    private final JTextField verificationCode = new JTextField();
    private final JTextField preparedBy = new JTextField();
    private final JTextField printedAt = new JTextField();
    private final JTextField checkedBy = new JTextField();
    private final JTextField receivedByName = new JTextField();
    private final JTextField receivedDate = new JTextField();
    private final JTextField subtotal = new JTextField();
    private final JTextField discount = new JTextField();
    private final JTextField freight = new JTextField();
    private final JTextField miscellaneous = new JTextField();
    private final JTextField tax = new JTextField();
    private final JTextField total = new JTextField();
    private final JTextField currency = new JTextField();
    private final JButton analyze = new JButton("Analyze with Azure AI");
    private final JButton reanalyze = new JButton("Re-analyze with Azure AI");
    private final JButton offline = new JButton("Run Offline OCR");
    private final JButton viewOcrText = new JButton("View OCR Text / Warnings");
    private final JButton addItem = new JButton("Add Item Row");
    private final JButton removeItem = new JButton("Remove Item Row");
    private final JButton matchProduct = new JButton("Match Product");
    private final JButton saveMapping = new JButton("Save Supplier Mapping");
    private final JButton approve = new JButton("Approve and Receive Stock");
    private final JButton cancel = new JButton("Cancel");

    private Path source;
    private ExtractedInvoice invoice;
    private InvoiceExtractionService activeService;
    private SwingWorker<?, ?> activeWorker;

    public InvoiceImportDialog(Window owner, Connection connection, Runnable onReceived) {
        super(owner, "Import Supplier Invoice", ModalityType.APPLICATION_MODAL);
        this.connection = connection;
        this.onReceived = onReceived == null ? () -> { } : onReceived;
        this.matchingService = connection == null ? null : new ProductMatchingService(connection);
        this.repository = connection == null ? null : new InvoiceImportRepository(connection);

        itemModel = new DefaultTableModel(ITEM_COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return column >= 1 && column <= 9;
            }
        };
        itemTable = new JTable(itemModel);
        buildUi();
        setState(false, false);
        setSize(1400, 820);
        if (owner != null) {
            setLocationRelativeTo(owner);
        } else {
            setLocationByPlatform(true);
        }
        SwingUtilities.invokeLater(() -> {
            try {
                if (connection != null) {
                    loadSuppliers();
                    showReadyStatus();
                } else {
                    setStatus("Database connection is unavailable. The import dialog can still be opened, but invoice receiving is disabled until the connection is restored.", new Color(180, 83, 9));
                }
            } catch (Exception ex) {
                setStatus("Invoice import dialog started with reduced functionality: " + ex.getMessage(), new Color(180, 83, 9));
            }
        });
    }

    private void buildUi() {
        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(245, 247, 250));
        ((javax.swing.JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel north = new JPanel(new BorderLayout(4, 2));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton choose = new JButton("Choose Invoice");
        choose.addActionListener(event -> chooseInvoice());
        analyze.setBackground(new Color(0, 120, 212));
        analyze.setForeground(Color.WHITE);
        analyze.setFont(analyze.getFont().deriveFont(Font.BOLD));
        analyze.setToolTipText("Cloud extraction using Azure Document Intelligence prebuilt-invoice.");
        analyze.addActionListener(event -> analyze(true));
        reanalyze.addActionListener(event -> analyze(true));
        offline.setToolTipText("Local Tesseract OCR. No invoice content is sent to Azure.");
        offline.addActionListener(event -> analyze(false));
        toolbar.add(choose);
        toolbar.add(analyze);
        toolbar.add(reanalyze);
        toolbar.add(offline);
        toolbar.add(selectedFile);
        progress.setIndeterminate(true);
        progress.setPreferredSize(new Dimension(150, 18));
        progress.setVisible(false);
        toolbar.add(progress);
        engineStatus.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 4));
        engineStatus.setFont(engineStatus.getFont().deriveFont(Font.BOLD));
        north.add(toolbar, BorderLayout.NORTH);
        north.add(engineStatus, BorderLayout.SOUTH);
        getContentPane().add(north, BorderLayout.NORTH);

        preview.setVerticalAlignment(SwingConstants.TOP);
        preview.setBorder(BorderFactory.createTitledBorder("Original invoice preview"));
        JScrollPane previewScroll = new JScrollPane(preview);
        previewScroll.setPreferredSize(new Dimension(390, 650));

        JPanel review = new JPanel(new BorderLayout(6, 6));
        review.add(buildHeaderPanel(), BorderLayout.NORTH);

        itemTable.setRowHeight(27);
        itemTable.setAutoCreateRowSorter(true);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                removeItem.setEnabled(activeWorker == null && invoice != null && itemTable.getSelectedRow() >= 0);
            }
        });
        itemTable.getColumnModel().getColumn(0).setCellRenderer(new StatusRenderer());
        itemTable.getColumnModel().getColumn(2).setPreferredWidth(240);
        itemTable.getColumnModel().getColumn(10).setPreferredWidth(180);
        review.add(new JScrollPane(itemTable), BorderLayout.CENTER);

        JPanel reviewBottom = new JPanel(new BorderLayout(5, 5));
        JPanel itemActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        matchProduct.addActionListener(event -> matchSelectedProduct());
        saveMapping.addActionListener(event -> markMappingForSave());
        viewOcrText.addActionListener(event -> showOcrDetails());
        addItem.addActionListener(event -> addManualItem());
        removeItem.addActionListener(event -> removeSelectedItem());
        itemActions.add(matchProduct);
        itemActions.add(saveMapping);
        itemActions.add(addItem);
        itemActions.add(removeItem);
        itemActions.add(viewOcrText);
        reviewBottom.add(itemActions, BorderLayout.NORTH);
        validationPane.setEditable(false);
        validationPane.setPreferredSize(new Dimension(500, 125));
        reviewBottom.add(new JScrollPane(validationPane), BorderLayout.CENTER);
        review.add(reviewBottom, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, previewScroll, review);
        split.setResizeWeight(0.28);
        getContentPane().add(split, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        approve.setBackground(new Color(22, 101, 52));
        approve.setForeground(Color.WHITE);
        approve.addActionListener(event -> approveAndReceive());
        cancel.addActionListener(event -> cancelOrClose());
        buttons.add(approve);
        buttons.add(cancel);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private JTabbedPane buildHeaderPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createTitledBorder("Extracted invoice details (editable)"));

        JPanel invoiceDetails = fieldPanel();
        addField(invoiceDetails, "Matched supplier", supplier);
        addField(invoiceDetails, "Document type", documentType);
        addField(invoiceDetails, "Copy type", copyType);
        addField(invoiceDetails, "Invoice number", invoiceNumber);
        addField(invoiceDetails, "Invoice date (yyyy-mm-dd)", invoiceDate);
        addField(invoiceDetails, "Due date (yyyy-mm-dd)", dueDate);
        addField(invoiceDetails, "Sales order number", salesOrderNumber);
        addField(invoiceDetails, "Order date (yyyy-mm-dd)", orderDate);
        addField(invoiceDetails, "Ship date (yyyy-mm-dd)", shipDate);
        addField(invoiceDetails, "Salesperson", salesperson);
        addField(invoiceDetails, "Customer purchase order", purchaseOrder);
        tabs.addTab("Invoice & Orders", invoiceDetails);

        JPanel parties = fieldPanel();
        addField(parties, "Extracted supplier", supplierName);
        addField(parties, "Supplier address", supplierAddress);
        addField(parties, "Supplier VAT registration", supplierVatRegistration);
        addField(parties, "Supplier TIN", supplierTaxId);
        addField(parties, "Supplier telephone", supplierTelephone);
        addField(parties, "Customer code", customerCode);
        addField(parties, "Customer name", customerName);
        addField(parties, "Customer address", customerAddress);
        addField(parties, "Customer TIN", customerTaxId);
        addField(parties, "Customer telephone", customerTelephone);
        addField(parties, "Shipping address", shippingAddress);
        addField(parties, "Shipping instructions", shippingInstructions);
        tabs.addTab("Supplier, Customer & Shipping", parties);

        JPanel fiscal = fieldPanel();
        addField(fiscal, "Fiscal document number (FDN)", fiscalDocumentNumber);
        addField(fiscal, "Verification code", verificationCode);
        addField(fiscal, "Prepared by", preparedBy);
        addField(fiscal, "Printed on / at", printedAt);
        addField(fiscal, "Checked by", checkedBy);
        addField(fiscal, "Received by (name)", receivedByName);
        addField(fiscal, "Received date (yyyy-mm-dd)", receivedDate);
        tabs.addTab("Fiscal & Approval", fiscal);

        JPanel totals = fieldPanel();
        addField(totals, "Total gross amount", subtotal);
        addField(totals, "Total discount", discount);
        addField(totals, "Total freight", freight);
        addField(totals, "Misc. charges", miscellaneous);
        addField(totals, "Total tax", tax);
        addField(totals, "Total invoice", total);
        addField(totals, "Currency", currency);
        tabs.addTab("Totals", totals);
        tabs.setPreferredSize(new Dimension(800, 235));
        return tabs;
    }

    private JPanel fieldPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 4, 6, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        return panel;
    }

    private void addField(JPanel panel, String label, Component field) {
        JLabel title = new JLabel(label + ":", SwingConstants.RIGHT);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(title);
        panel.add(field);
    }

    private void chooseInvoice() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a supplier invoice");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Invoices (PDF, JPG, PNG, TIFF, BMP, text)", "pdf", "jpg", "jpeg", "png", "tif", "tiff", "bmp", "txt"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        source = chooser.getSelectedFile().toPath();
        selectedFile.setText(source.getFileName().toString());
        showPreview(source.toFile());
        invoice = null;
        clearReview();
        setStatus("Invoice selected. Choose Azure AI or Offline OCR.", new Color(30, 64, 175));
        setState(false, false);
    }

    private void showPreview(File file) {
        preview.setIcon(null);
        if (file.getName().toLowerCase().endsWith(".pdf")) {
            preview.setText("<html><div style='text-align:center;padding:30px'>PDF selected<br><b>"
                    + escape(file.getName()) + "</b><br><br>The extracted fields and items will appear on the right.</div></html>");
            return;
        }
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) throw new IllegalArgumentException();
            double scale = Math.min(1.0, Math.min(360.0 / image.getWidth(), 560.0 / image.getHeight()));
            int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
            int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
            preview.setText("");
            preview.setIcon(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        } catch (Exception ex) {
            preview.setText("Preview unavailable for " + file.getName());
        }
    }

    private void analyze(boolean useAzure) {
        if (source == null) {
            JOptionPane.showMessageDialog(this, "Choose an invoice first.", "Invoice Import", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (useAzure && !AzureInvoiceExtractionService.isConfigured()) {
            setStatus("Azure AI is not configured for this session. Set the endpoint and key, then restart LuckyPOS.", new Color(180, 83, 9));
            JOptionPane.showMessageDialog(this,
                    "Azure Document Intelligence is not configured for this session. Set the endpoint and key, then restart LuckyPOS, or choose Offline OCR instead.",
                    "Azure Analysis", JOptionPane.WARNING_MESSAGE);
            return;
        }
        activeService = useAzure ? new AzureInvoiceExtractionService() : new OfflineInvoiceExtractionService();
        String engine = useAzure ? "Azure Document Intelligence" : "Offline Tesseract OCR";
        setStatus("Running " + engine + "...", new Color(30, 64, 175));
        setState(true, false);
        activeWorker = new SwingWorker<ExtractedInvoice, Void>() {
            @Override protected ExtractedInvoice doInBackground() throws Exception {
                ExtractedInvoice extracted = activeService.extract(source);
                if (matchingService != null) {
                    matchingService.match(extracted);
                }
                return extracted;
            }
            @Override protected void done() {
                try {
                    invoice = get();
                    displayInvoice();
                    int itemCount = invoice.getItems().size();
                    Color color = itemCount == 0 ? new Color(180, 83, 9) : new Color(21, 128, 61);
                    setStatus(engine + " completed: " + itemCount + " item row"
                            + (itemCount == 1 ? "" : "s") + " extracted. Review highlighted fields.", color);
                    if (itemCount == 0 && !value(invoice.getRawExtractedText()).isBlank()) {
                        JOptionPane.showMessageDialog(InvoiceImportDialog.this,
                                "OCR read text from the document, but no complete item rows were safe to import.\n"
                                + "Use 'View OCR Text / Warnings', retry Azure AI, or add rows manually.",
                                "OCR Needs Review", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (CancellationException ex) {
                    setStatus(engine + " was cancelled.", new Color(180, 83, 9));
                } catch (Exception ex) {
                    String message = userMessage(ex);
                    setStatus(engine + " failed: " + message, new Color(185, 28, 28));
                    JOptionPane.showMessageDialog(InvoiceImportDialog.this, message,
                            "Invoice Analysis", JOptionPane.ERROR_MESSAGE);
                } finally {
                    activeWorker = null;
                    activeService = null;
                    setState(false, invoice != null);
                    if (invoice != null) validateAndDisplay();
                }
            }
        };
        activeWorker.execute();
    }

    private void displayInvoice() {
        supplierName.setText(value(invoice.getSupplierName()));
        supplierAddress.setText(value(invoice.getSupplierAddress()));
        supplierVatRegistration.setText(value(invoice.getSupplierVatRegistrationNumber()));
        supplierTaxId.setText(value(invoice.getSupplierTaxId()));
        supplierTelephone.setText(value(invoice.getSupplierTelephone()));
        customerCode.setText(value(invoice.getCustomerCode()));
        customerName.setText(value(invoice.getCustomerName()));
        customerAddress.setText(value(invoice.getCustomerAddress()));
        customerTaxId.setText(value(invoice.getCustomerTaxId()));
        customerTelephone.setText(value(invoice.getCustomerTelephone()));
        shippingAddress.setText(value(invoice.getShippingAddress()));
        shippingInstructions.setText(value(invoice.getShippingInstructions()));
        documentType.setText(value(invoice.getDocumentType()));
        copyType.setText(value(invoice.getCopyType()));
        invoiceNumber.setText(value(invoice.getInvoiceNumber()));
        invoiceDate.setText(invoice.getInvoiceDate() == null ? "" : invoice.getInvoiceDate().toString());
        dueDate.setText(invoice.getDueDate() == null ? "" : invoice.getDueDate().toString());
        salesOrderNumber.setText(value(invoice.getSalesOrderNumber()));
        orderDate.setText(invoice.getOrderDate() == null ? "" : invoice.getOrderDate().toString());
        shipDate.setText(invoice.getShipDate() == null ? "" : invoice.getShipDate().toString());
        salesperson.setText(value(invoice.getSalesperson()));
        purchaseOrder.setText(value(invoice.getPurchaseOrderNumber()));
        fiscalDocumentNumber.setText(value(invoice.getFiscalDocumentNumber()));
        verificationCode.setText(value(invoice.getVerificationCode()));
        preparedBy.setText(value(invoice.getPreparedBy()));
        printedAt.setText(value(invoice.getPrintedAt()));
        checkedBy.setText(value(invoice.getCheckedBy()));
        receivedByName.setText(value(invoice.getReceivedByName()));
        receivedDate.setText(invoice.getReceivedDate() == null ? "" : invoice.getReceivedDate().toString());
        subtotal.setText(decimal(invoice.getSubtotal()));
        discount.setText(decimal(invoice.getDiscount()));
        freight.setText(decimal(invoice.getFreight()));
        miscellaneous.setText(decimal(invoice.getMiscellaneousCharges()));
        tax.setText(decimal(invoice.getTax()));
        total.setText(decimal(invoice.getTotal()));
        currency.setText(value(invoice.getCurrency()));
        viewOcrText.setEnabled(!value(invoice.getRawExtractedText()).isBlank()
                || !invoice.getExtractionWarnings().isEmpty());
        selectSupplier(invoice.getSupplierId());

        itemModel.setRowCount(0);
        for (ExtractedInvoiceItem item : invoice.getItems()) {
            itemModel.addRow(new Object[]{status(item), value(item.getSupplierProductCode()), value(item.getDescription()),
                value(item.getWarehouse()), decimal(item.getQuantity()), value(item.getUnit()), decimal(item.getUnitPrice()),
                decimal(item.getTax()), decimal(item.getDiscount()), decimal(item.getAmount()), matchedText(item),
                String.format("%.0f%%", item.getConfidence() * 100)});
        }
    }

    private void syncEdits() {
        if (invoice == null) return;
        SupplierChoice selected = (SupplierChoice) supplier.getSelectedItem();
        invoice.setSupplierId(selected == null || selected.supplierId() < 0 ? null : selected.supplierId());
        update(invoice.getSupplierName(), supplierName.getText(), "supplierName", invoice::setSupplierName);
        update(invoice.getSupplierAddress(), supplierAddress.getText(), "supplierAddress", invoice::setSupplierAddress);
        update(invoice.getSupplierVatRegistrationNumber(), supplierVatRegistration.getText(), "supplierVatRegistrationNumber", invoice::setSupplierVatRegistrationNumber);
        update(invoice.getSupplierTaxId(), supplierTaxId.getText(), "supplierTaxId", invoice::setSupplierTaxId);
        update(invoice.getSupplierTelephone(), supplierTelephone.getText(), "supplierTelephone", invoice::setSupplierTelephone);
        update(invoice.getCustomerCode(), customerCode.getText(), "customerCode", invoice::setCustomerCode);
        update(invoice.getCustomerName(), customerName.getText(), "customerName", invoice::setCustomerName);
        update(invoice.getCustomerAddress(), customerAddress.getText(), "customerAddress", invoice::setCustomerAddress);
        update(invoice.getCustomerTaxId(), customerTaxId.getText(), "customerTaxId", invoice::setCustomerTaxId);
        update(invoice.getCustomerTelephone(), customerTelephone.getText(), "customerTelephone", invoice::setCustomerTelephone);
        update(invoice.getShippingAddress(), shippingAddress.getText(), "shippingAddress", invoice::setShippingAddress);
        update(invoice.getShippingInstructions(), shippingInstructions.getText(), "shippingInstructions", invoice::setShippingInstructions);
        update(invoice.getDocumentType(), documentType.getText(), "documentType", invoice::setDocumentType);
        update(invoice.getCopyType(), copyType.getText(), "copyType", invoice::setCopyType);
        update(invoice.getInvoiceNumber(), invoiceNumber.getText(), "invoiceNumber", invoice::setInvoiceNumber);
        setDate(invoice.getInvoiceDate(), invoiceDate.getText(), "invoiceDate", invoice::setInvoiceDate);
        setDate(invoice.getDueDate(), dueDate.getText(), "dueDate", invoice::setDueDate);
        update(invoice.getSalesOrderNumber(), salesOrderNumber.getText(), "salesOrderNumber", invoice::setSalesOrderNumber);
        setDate(invoice.getOrderDate(), orderDate.getText(), "orderDate", invoice::setOrderDate);
        setDate(invoice.getShipDate(), shipDate.getText(), "shipDate", invoice::setShipDate);
        update(invoice.getSalesperson(), salesperson.getText(), "salesperson", invoice::setSalesperson);
        update(invoice.getPurchaseOrderNumber(), purchaseOrder.getText(), "purchaseOrderNumber", invoice::setPurchaseOrderNumber);
        update(invoice.getFiscalDocumentNumber(), fiscalDocumentNumber.getText(), "fiscalDocumentNumber", invoice::setFiscalDocumentNumber);
        update(invoice.getVerificationCode(), verificationCode.getText(), "verificationCode", invoice::setVerificationCode);
        update(invoice.getPreparedBy(), preparedBy.getText(), "preparedBy", invoice::setPreparedBy);
        update(invoice.getPrintedAt(), printedAt.getText(), "printedAt", invoice::setPrintedAt);
        update(invoice.getCheckedBy(), checkedBy.getText(), "checkedBy", invoice::setCheckedBy);
        update(invoice.getReceivedByName(), receivedByName.getText(), "receivedByName", invoice::setReceivedByName);
        setDate(invoice.getReceivedDate(), receivedDate.getText(), "receivedDate", invoice::setReceivedDate);
        setDecimal(invoice.getSubtotal(), subtotal.getText(), "subtotal", invoice::setSubtotal);
        setDecimal(invoice.getDiscount(), discount.getText(), "discount", invoice::setDiscount);
        setDecimal(invoice.getFreight(), freight.getText(), "freight", invoice::setFreight);
        setDecimal(invoice.getMiscellaneousCharges(), miscellaneous.getText(), "miscellaneousCharges", invoice::setMiscellaneousCharges);
        setDecimal(invoice.getTax(), tax.getText(), "tax", invoice::setTax);
        setDecimal(invoice.getTotal(), total.getText(), "total", invoice::setTotal);
        update(invoice.getCurrency(), currency.getText(), "currency", invoice::setCurrency);

        if (itemModel.getRowCount() != invoice.getItems().size()) throw new IllegalArgumentException("Invoice rows changed unexpectedly.");
        for (int row = 0; row < itemModel.getRowCount(); row++) {
            ExtractedInvoiceItem item = invoice.getItems().get(row);
            updateItem(item.getSupplierProductCode(), cell(row, 1), "supplierProductCode", item::setSupplierProductCode, item);
            updateItem(item.getDescription(), cell(row, 2), "description", item::setDescription, item);
            updateItem(item.getWarehouse(), cell(row, 3), "warehouse", item::setWarehouse, item);
            setItemDecimal(item.getQuantity(), cell(row, 4), "quantity", item::setQuantity, item);
            updateItem(item.getUnit(), cell(row, 5), "unit", item::setUnit, item);
            setItemDecimal(item.getUnitPrice(), cell(row, 6), "unitPrice", item::setUnitPrice, item);
            setItemDecimal(item.getTax(), cell(row, 7), "tax", item::setTax, item);
            setItemDecimal(item.getDiscount(), cell(row, 8), "discount", item::setDiscount, item);
            setItemDecimal(item.getAmount(), cell(row, 9), "amount", item::setAmount, item);
        }
    }

    private void validateAndDisplay() {
        try {
            syncEdits();
            InvoiceValidationResult result = validationService.validate(invoice, repository);
            Map<Integer, ValidationMessage.Severity> itemSeverity = new LinkedHashMap<>();
            StringBuilder html = new StringBuilder("<html><body style='font-family:Segoe UI;font-size:11px'>");
            for (ValidationMessage message : result.messages()) {
                String color = switch (message.severity()) { case GREEN -> "#15803d"; case YELLOW -> "#a16207"; case RED -> "#b91c1c"; };
                html.append("<div style='color:").append(color).append("'>- ")
                        .append(escape(message.message())).append("</div>");
                if (message.itemIndex() != null) {
                    itemSeverity.merge(message.itemIndex(), message.severity(), (left, right) ->
                            severityRank(left) >= severityRank(right) ? left : right);
                }
            }
            validationPane.setText(html.append("</body></html>").toString());
            for (int row = 0; row < invoice.getItems().size(); row++) {
                ValidationMessage.Severity severity = itemSeverity.get(row);
                itemModel.setValueAt(severity == null ? status(invoice.getItems().get(row)) : severity.name(), row, 0);
            }
            approve.setEnabled(!result.hasBlockingErrors() && activeWorker == null);
        } catch (Exception ex) {
            validationPane.setText("<html><body style='color:#b91c1c'>" + escape(ex.getMessage()) + "</body></html>");
            approve.setEnabled(false);
        }
    }

    private void matchSelectedProduct() {
        if (invoice == null || itemTable.getSelectedRow() < 0) return;
        try {
            syncEdits();
            int row = itemTable.convertRowIndexToModel(itemTable.getSelectedRow());
            String query = JOptionPane.showInputDialog(this, "Search by barcode or product name:",
                    invoice.getItems().get(row).getDescription());
            if (query == null) return;
            List<ProductMatch> products = matchingService.searchProducts(query);
            if (products.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No existing products matched that search.");
                return;
            }
            Map<String, ProductMatch> choices = new LinkedHashMap<>();
            for (ProductMatch product : products) choices.put("#" + product.productId() + "  " + product.name()
                    + "  [" + value(product.barcode()) + "]", product);
            Object selected = JOptionPane.showInputDialog(this, "Choose the internal product:", "Match Product",
                    JOptionPane.PLAIN_MESSAGE, null, choices.keySet().toArray(), choices.keySet().iterator().next());
            if (selected == null) return;
            matchingService.confirmManual(invoice.getItems().get(row), choices.get(selected.toString()), false);
            itemModel.setValueAt(matchedText(invoice.getItems().get(row)), row, 10);
            validateAndDisplay();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Product Matching", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markMappingForSave() {
        if (invoice == null || itemTable.getSelectedRow() < 0) return;
        int row = itemTable.convertRowIndexToModel(itemTable.getSelectedRow());
        ExtractedInvoiceItem item = invoice.getItems().get(row);
        if (!item.isMatched() || item.getSupplierProductCode() == null || item.getSupplierProductCode().isBlank()) {
            JOptionPane.showMessageDialog(this, "Match this item and confirm its supplier code first.");
            return;
        }
        item.setSaveSupplierMapping(true);
        JOptionPane.showMessageDialog(this, "This supplier-product mapping will be saved when the invoice is approved.");
    }

    private void showOcrDetails() {
        if (invoice == null) return;
        StringBuilder details = new StringBuilder();
        details.append("Method: ").append(value(invoice.getExtractionMethod())).append("\n");
        details.append("Confidence: ").append(String.format("%.0f%%", invoice.getOverallConfidence() * 100)).append("\n\n");
        if (!invoice.getExtractionWarnings().isEmpty()) {
            details.append("Warnings:\n");
            for (String warning : invoice.getExtractionWarnings()) details.append("- ").append(warning).append('\n');
            details.append('\n');
        }
        details.append("Raw OCR text:\n").append(value(invoice.getRawExtractedText()));
        JTextArea text = new JTextArea(details.toString(), 32, 105);
        text.setEditable(false);
        text.setCaretPosition(0);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(text), "OCR Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addManualItem() {
        if (invoice == null) {
            JOptionPane.showMessageDialog(this, "Run Azure AI or Offline OCR before adding an item row.");
            return;
        }
        syncEdits();
        ExtractedInvoiceItem item = new ExtractedInvoiceItem();
        item.setConfidence(0.0);
        invoice.getItems().add(item);
        itemModel.addRow(new Object[]{"RED", "", "", "", "", "", "", "", "", "", "Not matched", "Manual"});
        int row = itemModel.getRowCount() - 1;
        itemTable.setRowSelectionInterval(row, row);
        itemTable.scrollRectToVisible(itemTable.getCellRect(row, 1, true));
        validateAndDisplay();
    }

    private void removeSelectedItem() {
        if (invoice == null || itemTable.getSelectedRow() < 0) return;
        int row = itemTable.convertRowIndexToModel(itemTable.getSelectedRow());
        if (JOptionPane.showConfirmDialog(this, "Remove the selected invoice row from this import?",
                "Remove Item", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        invoice.getItems().remove(row);
        itemModel.removeRow(row);
        validateAndDisplay();
    }

    private void approveAndReceive() {
        validateAndDisplay();
        InvoiceValidationResult validation = validationService.validate(invoice, repository);
        if (validation.hasBlockingErrors()) return;
        if (JOptionPane.showConfirmDialog(this,
                "Receive all matched quantities into stock? This operation is transactional.",
                "Approve Supplier Invoice", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        setState(true, true);
        activeWorker = new SwingWorker<Long, Void>() {
            @Override protected Long doInBackground() throws Exception {
                return repository.receive(invoice, System.getProperty("user.name", "unknown"));
            }
            @Override protected void done() {
                try {
                    long id = get();
                    onReceived.run();
                    JOptionPane.showMessageDialog(InvoiceImportDialog.this,
                            "Supplier invoice received successfully (record #" + id + ").");
                    dispose();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(InvoiceImportDialog.this,
                            "Nothing was committed. " + cause.getMessage(), "Receive Stock", JOptionPane.ERROR_MESSAGE);
                    setState(false, true);
                    validateAndDisplay();
                } finally {
                    activeWorker = null;
                }
            }
        };
        activeWorker.execute();
    }

    private void cancelOrClose() {
        if (activeWorker != null) {
            if (activeService != null) activeService.cancel();
            activeWorker.cancel(true);
            activeWorker = null;
            activeService = null;
            setState(false, invoice != null);
            if (invoice != null) validateAndDisplay();
        } else {
            dispose();
        }
    }

    private void setState(boolean busy, boolean hasInvoice) {
        progress.setVisible(busy);
        analyze.setEnabled(!busy && source != null);
        reanalyze.setEnabled(!busy && source != null && hasInvoice);
        offline.setEnabled(!busy && source != null);
        matchProduct.setEnabled(!busy && hasInvoice);
        saveMapping.setEnabled(!busy && hasInvoice);
        addItem.setEnabled(!busy && hasInvoice);
        removeItem.setEnabled(!busy && hasInvoice && itemTable.getSelectedRow() >= 0);
        viewOcrText.setEnabled(!busy && hasInvoice && (!value(invoice == null ? null : invoice.getRawExtractedText()).isBlank()
                || invoice != null && !invoice.getExtractionWarnings().isEmpty()));
        if (busy || !hasInvoice) approve.setEnabled(false);
        cancel.setText(busy ? "Cancel Operation" : "Cancel");
    }

    private void showReadyStatus() {
        boolean azureReady = AzureInvoiceExtractionService.isConfigured();
        boolean offlineReady = TesseractOcrEngine.isInstalled();
        String message = "Azure AI: " + (azureReady ? "configured" : "not visible to this process - restart after configuration")
                + "    |    Offline OCR: " + (offlineReady ? "Tesseract detected" : "Tesseract not detected");
        setStatus(message, azureReady && offlineReady ? new Color(21, 128, 61) : new Color(180, 83, 9));
    }

    private void setStatus(String message, Color color) {
        engineStatus.setText(message == null || message.isBlank() ? " " : message);
        engineStatus.setForeground(color);
    }

    private void clearReview() {
        for (JTextField field : List.of(supplierName, supplierAddress, supplierVatRegistration, supplierTaxId,
                supplierTelephone, customerCode, customerName, customerAddress, customerTaxId, customerTelephone,
                shippingAddress, shippingInstructions, documentType, copyType, invoiceNumber, invoiceDate, dueDate,
                salesOrderNumber, orderDate, shipDate, salesperson, purchaseOrder, fiscalDocumentNumber,
                verificationCode, preparedBy, printedAt, checkedBy, receivedByName, receivedDate,
                subtotal, discount, freight, miscellaneous, tax, total, currency)) {
            field.setText("");
        }
        if (supplier.getItemCount() > 0) supplier.setSelectedIndex(0);
        itemModel.setRowCount(0);
        validationPane.setText("");
    }

    private void loadSuppliers() {
        try {
            supplier.removeAllItems();
            supplier.addItem(new SupplierChoice(-1, "-- Select supplier --"));
            for (SupplierChoice choice : matchingService.listSuppliers()) supplier.addItem(choice);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Suppliers could not be loaded: " + ex.getMessage(),
                    "Invoice Import", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void selectSupplier(Integer supplierId) {
        if (supplierId == null) return;
        for (int index = 0; index < supplier.getItemCount(); index++) {
            if (supplier.getItemAt(index).supplierId() == supplierId) {
                supplier.setSelectedIndex(index);
                return;
            }
        }
    }

    private String userMessage(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof InvoiceExtractionException extraction) {
                return value(extraction.getMessage());
            }
            if (current.getCause() == null || current.getCause() == current) {
                break;
            }
            current = current.getCause();
        }
        Throwable cause = failure == null ? null : failure.getCause();
        String message = cause == null ? null : value(cause.getMessage());
        if (message == null || message.isBlank()) {
            message = failure == null ? null : value(failure.getMessage());
        }
        return message == null || message.isBlank() ? "Unknown error" : message;
    }

    private String status(ExtractedInvoiceItem item) {
        if (!item.isMatched() || item.getQuantity() == null || item.getUnitPrice() == null || item.getAmount() == null) return "RED";
        return item.getConfidence() < 0.80 ? "YELLOW" : "GREEN";
    }
    private String matchedText(ExtractedInvoiceItem item) {
        if (item.getMatchingStatus() == ExtractedInvoiceItem.MatchingStatus.SUGGESTED_NAME)
            return "Suggested (confirm): " + value(item.getMatchedProductName());
        if (item.getMatchedProductId() == null) return item.getMatchingStatus() == ExtractedInvoiceItem.MatchingStatus.SUGGESTED_NAME
                ? "Suggested: " + value(item.getMatchedProductName()) : "Not matched";
        return "#" + item.getMatchedProductId() + " - " + value(item.getMatchedProductName());
    }
    private String cell(int row, int column) { Object value = itemModel.getValueAt(row, column); return value == null ? "" : value.toString().trim(); }
    private String value(String value) { return value == null ? "" : value; }
    private String decimal(BigDecimal value) { return value == null ? "" : value.stripTrailingZeros().toPlainString(); }
    private BigDecimal parseDecimal(String text) { return text == null || text.isBlank() ? null : new BigDecimal(text.trim().replace(",", "")); }
    private LocalDate parseDate(String text) { return text == null || text.isBlank() ? null : LocalDate.parse(text.trim()); }

    private void update(String oldValue, String newValue, String field, java.util.function.Consumer<String> setter) {
        String cleaned = newValue == null || newValue.isBlank() ? null : newValue.trim();
        if (!Objects.equals(oldValue, cleaned)) invoice.markCorrected(field);
        setter.accept(cleaned);
    }
    private void setDate(LocalDate oldValue, String text, String field, java.util.function.Consumer<LocalDate> setter) {
        LocalDate value = parseDate(text); if (!Objects.equals(oldValue, value)) invoice.markCorrected(field); setter.accept(value);
    }
    private void setDecimal(BigDecimal oldValue, String text, String field, java.util.function.Consumer<BigDecimal> setter) {
        BigDecimal value = parseDecimal(text); if (!same(oldValue, value)) invoice.markCorrected(field); setter.accept(value);
    }
    private void updateItem(String oldValue, String newValue, String field, java.util.function.Consumer<String> setter, ExtractedInvoiceItem item) {
        String cleaned = newValue == null || newValue.isBlank() ? null : newValue.trim();
        if (!Objects.equals(oldValue, cleaned)) item.markCorrected(field); setter.accept(cleaned);
    }
    private void setItemDecimal(BigDecimal oldValue, String text, String field, java.util.function.Consumer<BigDecimal> setter, ExtractedInvoiceItem item) {
        BigDecimal value = parseDecimal(text); if (!same(oldValue, value)) item.markCorrected(field); setter.accept(value);
    }
    private boolean same(BigDecimal left, BigDecimal right) {
        return left == null ? right == null : right != null && left.compareTo(right) == 0;
    }
    private String escape(String text) {
        return value(text).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    private int severityRank(ValidationMessage.Severity severity) {
        return switch (severity) { case GREEN -> 0; case YELLOW -> 1; case RED -> 2; };
    }

    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            if (!selected) {
                String status = value == null ? "RED" : value.toString();
                component.setBackground("GREEN".equals(status) ? new Color(220, 252, 231)
                        : "YELLOW".equals(status) ? new Color(254, 249, 195) : new Color(254, 226, 226));
                component.setForeground(Color.DARK_GRAY);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return component;
        }
    }
}

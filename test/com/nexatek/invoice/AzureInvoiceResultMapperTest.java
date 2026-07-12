package com.nexatek.invoice;

import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.json.JsonProviders;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AzureInvoiceResultMapperTest {

    @Test
    void mapsCompleteInvoiceAndMultipleDecimalItems() throws Exception {
        String json = """
            {"apiVersion":"2024-11-30","modelId":"prebuilt-invoice","stringIndexType":"textElements",
             "content":"invoice","pages":[],"documents":[{"docType":"invoice","confidence":0.97,"fields":{
               "VendorName":{"type":"string","valueString":"KANSAI PLASCON UGANDA LIMITED","content":"KANSAI PLASCON UGANDA LIMITED","confidence":0.96},
               "VendorAddress":{"type":"string","valueString":"Namanve Business Park","confidence":0.91},
               "VendorTaxId":{"type":"string","valueString":"1000026502","confidence":0.93},
               "VendorPhoneNumber":{"type":"phoneNumber","valuePhoneNumber":"200529801/4","confidence":0.89},
               "CustomerId":{"type":"string","valueString":"FRA63","confidence":0.92},
               "CustomerName":{"type":"string","valueString":"FRAMERA ENTERPRISES LIMITED","confidence":0.94},
               "CustomerAddress":{"type":"string","valueString":"KAMPALA","confidence":0.90},
               "CustomerTaxId":{"type":"string","valueString":"1015848921","confidence":0.90},
               "InvoiceId":{"type":"string","valueString":"M000881684","confidence":0.99},
               "InvoiceDate":{"type":"date","valueDate":"2026-07-10","confidence":0.98},
               "DueDate":{"type":"date","valueDate":"2026-08-10","confidence":0.88},
               "PurchaseOrder":{"type":"string","valueString":"BASES","confidence":0.86},
               "SubTotal":{"type":"currency","valueCurrency":{"amount":1119644,"currencyCode":"UGX"},"confidence":0.98},
               "TotalTax":{"type":"currency","valueCurrency":{"amount":201535,"currencyCode":"UGX"},"confidence":0.98},
               "InvoiceTotal":{"type":"currency","valueCurrency":{"amount":1321180,"currencyCode":"UGX"},"confidence":0.99},
               "Items":{"type":"array","valueArray":[
                 {"type":"object","valueObject":{
                   "ProductCode":{"type":"string","valueString":"50360-001","confidence":0.92},
                   "Description":{"type":"string","valueString":"VINYL SILK TINTING BASE WO 1LTR","confidence":0.96},
                   "Warehouse":{"type":"string","valueString":"FG","confidence":0.88},
                   "Quantity":{"type":"number","valueNumber":16,"confidence":0.99},
                   "Unit":{"type":"string","valueString":"EA","confidence":0.82},
                   "UnitPrice":{"type":"currency","valueCurrency":{"amount":14349.000,"currencyCode":"UGX"},"confidence":0.98},
                   "Amount":{"type":"currency","valueCurrency":{"amount":229584.00,"currencyCode":"UGX"},"confidence":0.99}}},
                 {"type":"object","valueObject":{
                   "ProductCode":{"type":"string","valueString":"A-200","confidence":0.90},
                   "Description":{"type":"string","valueString":"DECIMAL TEST","confidence":0.95},
                   "Quantity":{"type":"number","valueNumber":2.5,"confidence":0.97},
                   "UnitPrice":{"type":"number","valueNumber":1000.25,"confidence":0.96},
                   "Amount":{"type":"number","valueNumber":2500.625,"confidence":0.96}}}
               ],"confidence":0.95}
             }}]}
            """;
        AnalyzeResult result;
        try (var reader = JsonProviders.createReader(json)) {
            result = AnalyzeResult.fromJson(reader);
        }

        ExtractedInvoice invoice = new AzureInvoiceResultMapper().map(result);
        assertEquals("KANSAI PLASCON UGANDA LIMITED", invoice.getSupplierName());
        assertEquals("M000881684", invoice.getInvoiceNumber());
        assertEquals("FRA63", invoice.getCustomerCode());
        assertEquals("KAMPALA", invoice.getCustomerAddress());
        assertEquals("1015848921", invoice.getCustomerTaxId());
        assertEquals(new BigDecimal("1119644.0"), invoice.getSubtotal());
        assertEquals("UGX", invoice.getCurrency());
        assertEquals(2, invoice.getItems().size());
        assertEquals(0, new BigDecimal("16").compareTo(invoice.getItems().get(0).getQuantity()));
        assertEquals(0, new BigDecimal("14349").compareTo(invoice.getItems().get(0).getUnitPrice()));
        assertEquals("FG", invoice.getItems().get(0).getWarehouse());
        assertEquals(0, new BigDecimal("2.5").compareTo(invoice.getItems().get(1).getQuantity()));
        assertEquals(0, new BigDecimal("1000.25").compareTo(invoice.getItems().get(1).getUnitPrice()));
    }

    @Test
    void missingFieldsRemainUnknownInsteadOfBecomingZero() throws Exception {
        String json = """
            {"apiVersion":"2024-11-30","modelId":"prebuilt-invoice","stringIndexType":"textElements",
             "content":"","pages":[],"documents":[{"docType":"invoice","confidence":0.4,
             "fields":{"Items":{"type":"array","valueArray":[{"type":"object","valueObject":{
               "Description":{"type":"string","valueString":"UNKNOWN ITEM","confidence":0.4}}}]}}}]}
            """;
        AnalyzeResult result;
        try (var reader = JsonProviders.createReader(json)) { result = AnalyzeResult.fromJson(reader); }
        ExtractedInvoice invoice = new AzureInvoiceResultMapper().map(result);
        assertNull(invoice.getSupplierName());
        assertNull(invoice.getInvoiceNumber());
        assertNull(invoice.getTotal());
        assertNull(invoice.getItems().get(0).getQuantity());
        assertNull(invoice.getItems().get(0).getUnitPrice());
        assertNull(invoice.getItems().get(0).getAmount());
    }
}

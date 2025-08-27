package com.invoice.system.invoice_system.service.query;

import com.invoice.system.invoice_system.dto.InvoiceView;
import com.invoice.system.invoice_system.dto.PaymentView;

import java.util.List;
import java.util.Map;

public interface InvoiceQueryService {
    List<InvoiceView> getAllInvoices(Boolean isActive);

    List<Map<Long, List<PaymentView>>> getInvoicePaymentsByIds(List<Long> invoiceIds);
}
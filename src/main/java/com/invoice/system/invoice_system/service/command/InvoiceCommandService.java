package com.invoice.system.invoice_system.service.command;

import com.invoice.system.invoice_system.dto.InvoiceRequest;
import com.invoice.system.invoice_system.dto.InvoiceResponse;
import com.invoice.system.invoice_system.dto.PaymentRequest;
import com.invoice.system.invoice_system.dto.ProcessOverdueRequest;

import java.time.LocalDate;

public interface InvoiceCommandService {
    InvoiceResponse saveInvoice(InvoiceRequest invoiceRequest);
    void processPayment(Long id, PaymentRequest paymentRequest);
    void processOverdue(ProcessOverdueRequest processOverdueRequest, LocalDate overDueDate);
}

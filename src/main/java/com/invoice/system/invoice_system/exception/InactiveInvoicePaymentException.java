package com.invoice.system.invoice_system.exception;

public class InactiveInvoicePaymentException extends RuntimeException {
    public InactiveInvoicePaymentException(long invoiceId) {
        super("Attempted to process payment on inactive invoice: " + invoiceId);
    }
}

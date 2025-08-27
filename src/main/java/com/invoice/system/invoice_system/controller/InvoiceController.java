package com.invoice.system.invoice_system.controller;

import com.invoice.system.invoice_system.dto.*;
import com.invoice.system.invoice_system.service.command.InvoiceCommandService;
import com.invoice.system.invoice_system.service.query.InvoiceQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/invoices")
@Slf4j
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceQueryService invoiceQueryService;
    private final InvoiceCommandService invoiceCommandService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InvoiceResponse> saveInvoice(@RequestBody @Valid InvoiceRequest invoiceRequest) {
        log.info("Received request to create invoice: {}", invoiceRequest);
        final InvoiceResponse invoiceResponse = invoiceCommandService.saveInvoice(invoiceRequest);
        log.info("Invoice created successfully with ID: {}", invoiceResponse.id());
        return new ResponseEntity<>(invoiceResponse, HttpStatus.CREATED);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<InvoiceView>> getAllInvoices(@RequestParam(required = false) Boolean isActive) {
        log.info("Fetching all invoices. Active filter: {}", isActive);
        List<InvoiceView> allInvoices = invoiceQueryService.getAllInvoices(isActive);
        log.info("Found {} invoices", allInvoices.size());
        return new ResponseEntity<>(allInvoices, HttpStatus.OK);
    }

    @PostMapping(value = "/{id}/payment", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processPayment(@PathVariable("id") Long id, @RequestBody PaymentRequest paymentRequest) {
        log.info("Processing payment for invoice ID: {}, Payment data: {}", id, paymentRequest);
        invoiceCommandService.processPayment(id, paymentRequest);
        log.info("Payment processed successfully for invoice ID: {}", id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(value = "/process-overdue", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processOverdue(@RequestBody ProcessOverdueRequest processOverdueRequest, @RequestParam(required = false) LocalDate overDueDate) {
        log.info("Processing overdue invoices. Request: {}, Overdue Date: {}", processOverdueRequest, overDueDate);
        invoiceCommandService.processOverdue(processOverdueRequest, overDueDate);
        log.info("Overdue invoice processing completed.");
        return new ResponseEntity<>("Invoice overdue process is completed for invoices", HttpStatus.OK);
    }

    @PostMapping(value = "/payments", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<Long, List<PaymentView>>>> getPaymentsForInvoiceId(@RequestBody List<Long> invoiceIds) {
        log.info("Fetching payment data for invoice IDs: {}", invoiceIds);
        var paymentsData = invoiceQueryService.getInvoicePaymentsByIds(invoiceIds);
        log.info("Retrieved payments for {} invoices", paymentsData.size());
        return new ResponseEntity<>(paymentsData, HttpStatus.OK);
    }
}

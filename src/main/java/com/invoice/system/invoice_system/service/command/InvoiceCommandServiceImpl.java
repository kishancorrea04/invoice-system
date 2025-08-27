package com.invoice.system.invoice_system.service.command;

import com.invoice.system.invoice_system.dto.InvoiceRequest;
import com.invoice.system.invoice_system.dto.InvoiceResponse;
import com.invoice.system.invoice_system.dto.PaymentRequest;
import com.invoice.system.invoice_system.dto.ProcessOverdueRequest;
import com.invoice.system.invoice_system.entity.InvoiceEntity;
import com.invoice.system.invoice_system.entity.PaymentEntity;
import com.invoice.system.invoice_system.exception.InactiveInvoicePaymentException;
import com.invoice.system.invoice_system.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InvoiceCommandServiceImpl implements InvoiceCommandService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public InvoiceResponse saveInvoice(InvoiceRequest invoiceRequest) {
        log.info("Saving new invoice with amount: {}, due date: {}", invoiceRequest.getAmount(), invoiceRequest.getDue_date());
        final InvoiceEntity invoiceEntity = new InvoiceEntity(invoiceRequest.getAmount(), invoiceRequest.getDue_date());
        final InvoiceEntity saveResponse = invoiceRepository.save(invoiceEntity);
        log.info("Invoice saved successfully with ID: {}", saveResponse.getId());
        return new InvoiceResponse(saveResponse.getId());
    }

    @Override
    public void processPayment(Long invoiceId, PaymentRequest paymentRequest) {
        log.info("Processing payment for invoice ID: {} with amount: {}", invoiceId, paymentRequest.getAmount());
        InvoiceEntity invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> {
                    log.error("Invoice not found with ID: {}", invoiceId);
                    return new EntityNotFoundException("Invoice not found with ID: " + invoiceId);
                });

        if (!invoice.isActive()) {
            log.warn("Attempted to process payment on inactive invoice ID: {}", invoiceId);
            throw new InactiveInvoicePaymentException(invoiceId);
        }

        applyPayment(invoice, paymentRequest.getAmount());
        invoiceRepository.save(invoice);
        log.info("Payment processed and invoice updated for ID: {}", invoiceId);
    }

    @Override
    public void processOverdue(ProcessOverdueRequest request, LocalDate overDueDate) {
        if (Objects.isNull(overDueDate)) {
            overDueDate = LocalDate.now();
            log.info("Overdue date not provided, defaulting to current date: {}", overDueDate);
        }

        log.info("Processing overdue invoices with date: {} and request: {}", overDueDate, request);
        List<InvoiceEntity> overdueInvoices = invoiceRepository.findAllOverdues(overDueDate);
        log.info("Found {} overdue invoices to process.", overdueInvoices.size());

        final LocalDate finalOverDueDate = overDueDate;
        overdueInvoices.forEach(invoice -> processOverdueInvoice(invoice, request, finalOverDueDate));
        log.info("Overdue invoice processing completed.");
    }

    private void processOverdueInvoice(InvoiceEntity invoice, ProcessOverdueRequest request, LocalDate today) {
        log.debug("Processing overdue invoice ID: {}", invoice.getId());

        BigDecimal paidAmount = invoice.getPaid_amount();
        BigDecimal totalAmount = invoice.getAmount();

        if (!invoice.isActive()) {
            log.debug("Skipping inactive invoice ID: {}", invoice.getId());
            return;
        }
        if (InvoiceEntity.Status.VOID.equals(invoice.getStatus())) {
            log.debug("Skipping VOID status invoice ID: {}", invoice.getId());
            return;
        }
        if (today.isBefore(invoice.getDue_date())) {
            log.debug("Skipping invoice ID: {} as it is not yet due", invoice.getId());
            return;
        }

        boolean isFullyPaid = paidAmount.compareTo(totalAmount) >= 0;
        boolean hasPartialPayment = paidAmount.compareTo(BigDecimal.ZERO) > 0;

        if (isFullyPaid) {
            log.info("Invoice ID: {} is fully paid. Marking as PAID.", invoice.getId());
            updateInvoiceStatus(invoice, InvoiceEntity.Status.PAID);
        } else if (hasPartialPayment) {
            log.info("Invoice ID: {} has partial payment. Creating new invoice for remaining amount.", invoice.getId());
            updateInvoiceStatus(invoice, InvoiceEntity.Status.PAID);
            createNewInvoice(invoice, totalAmount.subtract(paidAmount), request, today);
        } else {
            log.info("Invoice ID: {} has no payments. Marking as VOID and generating new invoice.", invoice.getId());
            updateInvoiceStatus(invoice, InvoiceEntity.Status.VOID);
            createNewInvoice(invoice, totalAmount, request, today);
        }
    }

    private void updateInvoiceStatus(InvoiceEntity invoice, InvoiceEntity.Status status) {
        log.debug("Updating status for invoice ID: {} to {}", invoice.getId(), status);
        invoice.setStatus(status);
        invoice.setActive(false);
        invoiceRepository.save(invoice);
    }

    private void createNewInvoice(InvoiceEntity invoice, BigDecimal baseAmount, ProcessOverdueRequest request, LocalDate today) {
        BigDecimal newAmount = baseAmount.add(request.getLate_fee());
        LocalDate newDueDate = today.plusDays(request.getOverdue_days());

        log.info("Creating new invoice from parent ID: {} with amount: {}, due date: {}", invoice.getId(), newAmount, newDueDate);

        InvoiceEntity newInvoice = new InvoiceEntity(newAmount, newDueDate);
        newInvoice.setParentInvoiceId(invoice.getId());
        invoiceRepository.save(newInvoice);

        log.info("New invoice created with parent ID: {}", invoice.getId());
    }

    private void applyPayment(InvoiceEntity invoice, BigDecimal amount) {
        log.debug("Applying payment of amount: {} to invoice ID: {}", amount, invoice.getId());

        PaymentEntity payment = new PaymentEntity(amount);
        payment.setInvoice(invoice);

        invoice.getPaymentEntities().add(payment);
        invoice.setPaid_amount(invoice.getPaid_amount().add(amount));

        if (invoice.getPaid_amount().compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus(InvoiceEntity.Status.PAID);
        }

        log.debug("Payment applied. New paid amount: {}", invoice.getPaid_amount());
    }
}

package com.invoice.system.invoice_system.service.command;

import com.invoice.system.invoice_system.dto.InvoiceRequest;
import com.invoice.system.invoice_system.dto.InvoiceResponse;
import com.invoice.system.invoice_system.dto.PaymentRequest;
import com.invoice.system.invoice_system.dto.ProcessOverdueRequest;
import com.invoice.system.invoice_system.entity.InvoiceEntity;
import com.invoice.system.invoice_system.exception.InactiveInvoicePaymentException;
import com.invoice.system.invoice_system.repository.InvoiceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceCommandServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceCommandServiceImpl invoiceCommandService;

    private InvoiceEntity activeInvoice;
    private InvoiceEntity inactiveInvoice;

    @BeforeEach
    void setup() {
        activeInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().plusDays(5));
        activeInvoice.setId(1L);
        activeInvoice.setActive(true);
        activeInvoice.setPaid_amount(BigDecimal.ZERO);
        activeInvoice.setPaymentEntities(new ArrayList<>());

        inactiveInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().plusDays(5));
        inactiveInvoice.setId(2L);
        inactiveInvoice.setActive(false);
        inactiveInvoice.setPaid_amount(BigDecimal.ZERO);
        inactiveInvoice.setPaymentEntities(new ArrayList<>());
    }

    // ===== saveInvoice =====

    @Test
    void saveInvoice_shouldSaveAndReturnResponse() {
        InvoiceRequest request = new InvoiceRequest(BigDecimal.valueOf(100), LocalDate.now().plusDays(10));

        InvoiceEntity savedInvoice = new InvoiceEntity(request.getAmount(), request.getDue_date());
        savedInvoice.setId(123L);

        when(invoiceRepository.save(any(InvoiceEntity.class))).thenReturn(savedInvoice);

        InvoiceResponse response = invoiceCommandService.saveInvoice(request);

        assertNotNull(response);
        assertEquals(123L, response.id());
        verify(invoiceRepository).save(any(InvoiceEntity.class));
    }

    // ===== processPayment =====

    @Test
    void processPayment_shouldThrowExceptionIfInvoiceNotFound() {
        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> invoiceCommandService.processPayment(99L, new PaymentRequest(BigDecimal.TEN)));

        assertTrue(ex.getMessage().contains("Invoice not found"));
    }

    @Test
    void processPayment_shouldThrowExceptionIfInvoiceInactive() {
        when(invoiceRepository.findById(inactiveInvoice.getId())).thenReturn(Optional.of(inactiveInvoice));

        InactiveInvoicePaymentException ex = assertThrows(InactiveInvoicePaymentException.class,
                () -> invoiceCommandService.processPayment(inactiveInvoice.getId(), new PaymentRequest(BigDecimal.TEN)));
        Assertions.assertThat(ex).isNotNull()
                .hasMessageContaining("Attempted to process payment on inactive invoice");
    }

    @Test
    void processPayment_shouldApplyPaymentAndSave() {
        when(invoiceRepository.findById(activeInvoice.getId())).thenReturn(Optional.of(activeInvoice));
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal paymentAmount = BigDecimal.valueOf(50);
        PaymentRequest paymentRequest = new PaymentRequest(paymentAmount);

        invoiceCommandService.processPayment(activeInvoice.getId(), paymentRequest);

        // Verify payment was added
        assertEquals(1, activeInvoice.getPaymentEntities().size());
        assertEquals(paymentAmount, activeInvoice.getPaid_amount());
        assertEquals(InvoiceEntity.Status.PAID, activeInvoice.getStatus());
        verify(invoiceRepository).save(activeInvoice);
    }

    // ===== processOverdue =====

    @Test
    void processOverdue_shouldDefaultDateToTodayIfNull() {
        ProcessOverdueRequest request = new ProcessOverdueRequest(BigDecimal.TEN, 5);
        List<InvoiceEntity> overdueInvoices = List.of(activeInvoice);

        when(invoiceRepository.findAllOverdues(any(LocalDate.class))).thenReturn(overdueInvoices);

        invoiceCommandService.processOverdue(request, null);

        verify(invoiceRepository).findAllOverdues(LocalDate.now());
        verify(invoiceRepository, times(0)).save(any());
    }

    @Test
    void processOverdue_shouldProcessMultipleInvoicesCorrectly() {
        ProcessOverdueRequest request = new ProcessOverdueRequest(BigDecimal.TEN, 5);

        InvoiceEntity fullyPaidInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().minusDays(1));
        fullyPaidInvoice.setId(10L);
        fullyPaidInvoice.setActive(true);
        fullyPaidInvoice.setPaid_amount(BigDecimal.valueOf(100));
        fullyPaidInvoice.setPaymentEntities(new ArrayList<>());

        InvoiceEntity partiallyPaidInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().minusDays(1));
        partiallyPaidInvoice.setId(11L);
        partiallyPaidInvoice.setActive(true);
        partiallyPaidInvoice.setPaid_amount(BigDecimal.valueOf(50));
        partiallyPaidInvoice.setPaymentEntities(new ArrayList<>());

        InvoiceEntity noPaymentInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().minusDays(1));
        noPaymentInvoice.setId(12L);
        noPaymentInvoice.setActive(true);
        noPaymentInvoice.setPaid_amount(BigDecimal.ZERO);
        noPaymentInvoice.setPaymentEntities(new ArrayList<>());

        InvoiceEntity voidStatusInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().minusDays(1));
        voidStatusInvoice.setId(13L);
        voidStatusInvoice.setActive(true);
        voidStatusInvoice.setPaid_amount(BigDecimal.ZERO);
        voidStatusInvoice.setStatus(InvoiceEntity.Status.VOID);
        voidStatusInvoice.setPaymentEntities(new ArrayList<>());

        InvoiceEntity inactiveInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().minusDays(1));
        inactiveInvoice.setId(14L);
        inactiveInvoice.setActive(false);
        inactiveInvoice.setPaid_amount(BigDecimal.ZERO);
        inactiveInvoice.setPaymentEntities(new ArrayList<>());

        List<InvoiceEntity> invoices = List.of(
                fullyPaidInvoice,
                partiallyPaidInvoice,
                noPaymentInvoice,
                voidStatusInvoice,
                inactiveInvoice
        );

        when(invoiceRepository.findAllOverdues(any(LocalDate.class))).thenReturn(invoices);
        when(invoiceRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        invoiceCommandService.processOverdue(request, LocalDate.now());

        // Fully paid invoice should be marked PAID & inactive
        assertEquals(InvoiceEntity.Status.PAID, fullyPaidInvoice.getStatus());
        assertFalse(fullyPaidInvoice.isActive());

        // Partially paid invoice marked PAID & inactive + new invoice created
        assertEquals(InvoiceEntity.Status.PAID, partiallyPaidInvoice.getStatus());
        assertFalse(partiallyPaidInvoice.isActive());

        // No payment invoice marked VOID & inactive + new invoice created
        assertEquals(InvoiceEntity.Status.VOID, noPaymentInvoice.getStatus());
        assertFalse(noPaymentInvoice.isActive());

        // VOID status invoice & inactive invoice are unchanged
        assertEquals(InvoiceEntity.Status.VOID, voidStatusInvoice.getStatus());
        assertTrue(voidStatusInvoice.isActive());

        assertFalse(inactiveInvoice.isActive());

        verify(invoiceRepository, atLeast(invoices.size())).save(any());
    }

    @Test
    void processOverdue_shouldSkipNotDueInvoices() {
        ProcessOverdueRequest request = new ProcessOverdueRequest(BigDecimal.TEN, 5);

        InvoiceEntity futureDueInvoice = new InvoiceEntity(BigDecimal.valueOf(100), LocalDate.now().plusDays(5));
        futureDueInvoice.setId(20L);
        futureDueInvoice.setActive(true);
        futureDueInvoice.setPaid_amount(BigDecimal.ZERO);
        futureDueInvoice.setPaymentEntities(new ArrayList<>());

        when(invoiceRepository.findAllOverdues(any(LocalDate.class))).thenReturn(List.of(futureDueInvoice));

        invoiceCommandService.processOverdue(request, LocalDate.now());

        // No status change for invoice not due yet
        assertTrue(futureDueInvoice.isActive());

        verify(invoiceRepository, never()).save(futureDueInvoice);
    }
}

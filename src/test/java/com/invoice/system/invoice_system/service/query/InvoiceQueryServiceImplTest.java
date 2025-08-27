package com.invoice.system.invoice_system.service.query;

import com.invoice.system.invoice_system.dto.InvoiceView;
import com.invoice.system.invoice_system.dto.PaymentView;
import com.invoice.system.invoice_system.entity.InvoiceEntity;
import com.invoice.system.invoice_system.entity.PaymentEntity;
import com.invoice.system.invoice_system.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceQueryServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Spy
    @InjectMocks
    private InvoiceQueryServiceImpl invoiceQueryService;

    private InvoiceEntity invoice1;
    private InvoiceEntity invoice2;
    private PaymentEntity payment1;
    private PaymentEntity payment2;

    @BeforeEach
    void setUp() {
        payment1 = new PaymentEntity(BigDecimal.valueOf(100));
        payment1.setCreatedAt(LocalDateTime.now());

        payment2 = new PaymentEntity(BigDecimal.valueOf(50));
        payment2.setCreatedAt(LocalDateTime.now());

        invoice1 = new InvoiceEntity();
        invoice1.setId(1L);
        invoice1.setPaymentEntities(new ArrayList<>(List.of(payment1)));

        invoice2 = new InvoiceEntity();
        invoice2.setId(2L);
        invoice2.setPaymentEntities(new ArrayList<>(List.of(payment2)));

        // link payments back to invoices
        payment1.setInvoice(invoice1);
        payment2.setInvoice(invoice2);
    }

    @Test
    void testGetAllInvoices_activeTrue_returnsList() {
        when(invoiceRepository.findAllByIsActive(true)).thenReturn(List.of(invoice1, invoice2));

        List<InvoiceView> result = invoiceQueryService.getAllInvoices(true);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(invoice2.getId(), result.get(0).getId()); // reverse sorted by id
        verify(invoiceRepository).findAllByIsActive(true);
    }

    @Test
    void testGetAllInvoices_activeFalse_returnsList() {
        when(invoiceRepository.findAllByIsActive(false)).thenReturn(List.of(invoice2));

        List<InvoiceView> result = invoiceQueryService.getAllInvoices(false);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceRepository).findAllByIsActive(false);
    }

    @Test
    void testGetAllInvoices_activeNull_returnsAll() {
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice1, invoice2));

        List<InvoiceView> result = invoiceQueryService.getAllInvoices(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void testGetAllInvoices_emptyList_returnsEmptyList() {
        when(invoiceRepository.findAllByIsActive(anyBoolean())).thenReturn(Collections.emptyList());

        List<InvoiceView> result = invoiceQueryService.getAllInvoices(true);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetInvoicePaymentsByIds_nullIds_fetchAll() {
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice1, invoice2));

        List<Map<Long, List<PaymentView>>> result = invoiceQueryService.getInvoicePaymentsByIds(null);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void testGetInvoicePaymentsByIds_emptyIds_fetchAll() {
        when(invoiceRepository.findAll()).thenReturn(List.of(invoice1));

        List<Map<Long, List<PaymentView>>> result = invoiceQueryService.getInvoicePaymentsByIds(Collections.emptyList());

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void testGetInvoicePaymentsByIds_withIds_found() {
        List<Long> ids = List.of(1L, 2L);
        when(invoiceRepository.findByIdIn(ids)).thenReturn(List.of(invoice1, invoice2));

        List<Map<Long, List<PaymentView>>> result = invoiceQueryService.getInvoicePaymentsByIds(ids);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(invoiceRepository).findByIdIn(ids);
    }

    @Test
    void testGetInvoicePaymentsByIds_withIds_notFound_returnsEmptyList() {
        List<Long> ids = List.of(3L);
        when(invoiceRepository.findByIdIn(ids)).thenReturn(Collections.emptyList());

        List<Map<Long, List<PaymentView>>> result = invoiceQueryService.getInvoicePaymentsByIds(ids);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(invoiceRepository).findByIdIn(ids);
    }

    @Test
    void testGetInvoicePaymentsByIds_invoicesWithoutPayments_areFilteredOut() {
        InvoiceEntity invoiceNoPayments = new InvoiceEntity();
        invoiceNoPayments.setId(3L);
        invoiceNoPayments.setPaymentEntities(Collections.emptyList());

        when(invoiceRepository.findByIdIn(anyList())).thenReturn(List.of(invoiceNoPayments));

        List<Map<Long, List<PaymentView>>> result = invoiceQueryService.getInvoicePaymentsByIds(List.of(3L));

        assertNotNull(result);
        assertTrue(result.isEmpty()); // because payments are empty and filtered out
    }

}
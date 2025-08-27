package com.invoice.system.invoice_system.service.query;

import com.invoice.system.invoice_system.dto.InvoiceView;
import com.invoice.system.invoice_system.dto.PaymentView;
import com.invoice.system.invoice_system.entity.InvoiceEntity;
import com.invoice.system.invoice_system.entity.PaymentEntity;
import com.invoice.system.invoice_system.repository.InvoiceRepository;
import com.invoice.system.invoice_system.util.ObjectMapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceQueryServiceImpl implements InvoiceQueryService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public List<InvoiceView> getAllInvoices(Boolean isActive) {
        log.info("Fetching all invoices. Active filter: {}", isActive);
        List<InvoiceEntity> allInvoices;

        if (Objects.nonNull(isActive)) {
            allInvoices = invoiceRepository.findAllByIsActive(isActive);
        } else {
            allInvoices = invoiceRepository.findAll();
        }

        if (CollectionUtils.isEmpty(allInvoices)) {
            log.warn("No invoices found for active={}", isActive);
            return Collections.emptyList();
        }

        List<InvoiceView> invoiceViews = ObjectMapperUtil.readValue(
                ObjectMapperUtil.writeValueAsString(allInvoices),
                InvoiceView.class,
                List.class
        );

        invoiceViews.sort(Comparator.comparing(InvoiceView::getId, Collections.reverseOrder()));
        log.info("Fetched {} invoices", invoiceViews.size());

        return invoiceViews;
    }

    @Override
    public List<Map<Long, List<PaymentView>>> getInvoicePaymentsByIds(List<Long> invoiceIds) {
        log.info("Fetching payment data for invoice IDs: {}", invoiceIds);

        List<InvoiceEntity> invoices = CollectionUtils.isEmpty(invoiceIds)
                ? invoiceRepository.findAll()
                : invoiceRepository.findByIdIn(invoiceIds);

        if (CollectionUtils.isEmpty(invoices)) {
            log.warn("No invoices found for payment fetch. Invoice IDs: {}", invoiceIds);
            return Collections.emptyList();
        }

        Map<Long, List<InvoiceEntity>> invoicesGroupedById = invoices.stream()
                .collect(groupingBy(InvoiceEntity::getId));

        List<Map<Long, List<PaymentView>>> result = invoicesGroupedById.entrySet().stream()
                .map(entry -> toInvoicePaymentMap(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .toList();

        log.info("Returning payment data for {} invoices", result.size());
        return result;
    }

    private Map<Long, List<PaymentView>> toInvoicePaymentMap(Long invoiceId, List<InvoiceEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            log.debug("No invoice entities found for ID: {}", invoiceId);
            return null;
        }

        InvoiceEntity invoice = entities.get(0); // assuming only one per ID
        List<PaymentEntity> payments = invoice.getPaymentEntities();

        if (CollectionUtils.isEmpty(payments)) {
            log.info("No payments found for invoice ID: {}", invoiceId);
            return null;
        }

        List<PaymentView> paymentViews = ObjectMapperUtil.readValue(
                ObjectMapperUtil.writeValueAsString(payments),
                PaymentView.class,
                List.class
        );

        log.debug("Mapped {} payments for invoice ID: {}", paymentViews.size(), invoiceId);
        return Map.of(invoiceId, paymentViews);
    }
}

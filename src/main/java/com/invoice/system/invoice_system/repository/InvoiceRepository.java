package com.invoice.system.invoice_system.repository;

import com.invoice.system.invoice_system.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {
    @Query("SELECT i FROM InvoiceEntity i WHERE i.due_date < :today")
    List<InvoiceEntity> findAllOverdues(LocalDate today);

    List<InvoiceEntity> findByIdIn(List<Long> invoiceIds);
    List<InvoiceEntity> findAllByIsActive(boolean isActive);
}

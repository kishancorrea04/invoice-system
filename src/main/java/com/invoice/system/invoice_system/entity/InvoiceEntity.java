package com.invoice.system.invoice_system.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class InvoiceEntity {
    public enum Status {PENDING, PAID, VOID}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paid_amount = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate due_date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "parent_invoice_id")
    private Long parentInvoiceId;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PaymentEntity> paymentEntities = new ArrayList<>();

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public InvoiceEntity(BigDecimal amount, LocalDate dueDate) {
        this.amount = amount;
        this.due_date = dueDate;
        this.createdAt = LocalDateTime.now();
    }
}
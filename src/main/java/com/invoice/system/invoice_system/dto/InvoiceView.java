package com.invoice.system.invoice_system.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceView {
    private long id;
    private BigDecimal amount;
    private BigDecimal paid_amount;
    private LocalDate due_date;
    private String status;
    private Long parentInvoiceId; // uncomment if we want to know for which id new invoice created
}
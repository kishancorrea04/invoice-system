package com.invoice.system.invoice_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceRequest {
    @NotNull
    private BigDecimal amount;
    @NotNull
    private LocalDate due_date;
}
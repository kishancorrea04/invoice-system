package com.invoice.system.invoice_system.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProcessOverdueRequest {
    private BigDecimal late_fee;
    private int overdue_days;
}

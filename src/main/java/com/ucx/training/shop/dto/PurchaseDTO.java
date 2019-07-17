package com.ucx.training.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDTO {
    private Integer costumerId;
    private Integer invoiceId;
}

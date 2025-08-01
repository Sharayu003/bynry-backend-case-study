package com.stockflow.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private String name;
    private String sku;
    private BigDecimal price;
    private Long warehouseId;
    private int initialQuantity;
}

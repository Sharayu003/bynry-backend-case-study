package com.stockflow.inventory.controller;

import com.stockflow.inventory.dto.ProductDTO;
import com.stockflow.inventory.entity.Product;
import com.stockflow.inventory.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO dto) {
        try {
            Product product = productService.createProduct(dto);
            return ResponseEntity.status(201).body(Map.of(
                    "message", "Product created",
                    "product_id", product.getId()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Something went wrong"));
        }
    }
}

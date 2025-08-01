package com.stockflow.inventory.service;

import com.stockflow.inventory.dto.ProductDTO;
import com.stockflow.inventory.entity.Inventory;
import com.stockflow.inventory.entity.Product;
import com.stockflow.inventory.repository.InventoryRepository;
import com.stockflow.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;

    public ProductService(ProductRepository productRepo, InventoryRepository inventoryRepo) {
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
    }

    @Transactional
    public Product createProduct(ProductDTO dto) {
        if (productRepo.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("SKU already exists.");
        }

        Product product = new Product(dto.getName(), dto.getSku(), dto.getPrice());
        productRepo.save(product);

        Inventory inventory = new Inventory();
        inventory.setWarehouseId(dto.getWarehouseId());
        inventory.setQuantity(dto.getInitialQuantity());
        inventory.setProduct(product);
        inventoryRepo.save(inventory);

        return product;
    }
}

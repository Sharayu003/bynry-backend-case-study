StockFlow - Java Spring Boot Debugging Documentation
Candidate: Sharayu Yeole
Part 1: Code Review & Debugging
Technology: Java + Spring Boot
Submission : 1/08/2025 

Problem Summary
The original endpoint was meant to create a new product and its initial inventory entry. However, the logic is flawed in various ways—both technical and business-related. The code compiles, but fails to function reliably in production.

1. Issues Identified
a. No Input Validation
Problem: The original code accepts user input without checking for nulls or missing fields.
Impact: Leads to runtime exceptions, inconsistent data, and invalid database entries.
b. SKU Uniqueness Not Enforced
Problem: There's no check to ensure SKUs are unique.
Impact: Duplicate SKUs could result in confusion during lookup or reporting.
c. Product ID Access Before DB Commit
Problem: Product ID (product.id) is used before it is guaranteed to be generated.
Impact: Might return null or cause integrity issues during inventory creation.
d. Lack of Error Handling
Problem: No try-catch block to handle exceptions.
Impact: A single failure can crash the endpoint or return unhelpful responses.
e. No Transactional Context
Problem: Product and Inventory are committed in separate transactions.
Impact: Product might be saved even if inventory fails, leading to partial data.
f. Mixing Concerns in Controller
Problem: Business logic is hardcoded in the controller.
Impact: Violates separation of concerns; harder to test and maintain.
2. Corrected Java Spring Boot Implementation

DTO Class: ProductRequest.java
public class ProductRequest {
    private String name;
    private String sku;
    private Double price;
    private Long warehouseId;
    private Integer initialQuantity;
    // Getters and setters
}

Controller: ProductController.java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest request) {
        try {
            Product product = productService.createProductWithInventory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Product created", "product_id", product.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Something went wrong"));
        }
    }
}

Service Layer: ProductService.java
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public Product createProductWithInventory(ProductRequest request) {

        if (request.getSku() == null || request.getSku().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }

        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists");
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setPrice(request.getPrice());
        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouseId(request.getWarehouseId());
        inventory.setQuantity(request.getInitialQuantity());
        inventoryRepository.save(inventory);

        return product;
    }
}

Reasoning Behind Each Fix
Input validation : Prevents invalid or null input data
SKU uniqueness check : Avoids duplicate SKUs, ensuring lookup integrity
Save product before accessing ID : Guarantees valid foreign key in inventory
Use @ Transactional : Rolls back both operations on failure 
Move logic to service layer : Enhances separation of concerns
Use DTO : clean API boundary and flexibility
 4. Assumptions
SKUs are globally unique across all products.
Each product starts with one initial warehouse entry.
Product-to-inventory is a one-to-many relationship.
Additional product attributes like description or category are optional.
created_at and updated_at fields are managed automatically.

Outcome
The corrected implementation ensures consistency, transactional safety, and modular design.
Code adheres to best practices for maintainability and scalability.
System is now robust enough to integrate with future modules like the low-stock alerting system.


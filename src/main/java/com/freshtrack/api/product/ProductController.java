package com.freshtrack.api.product;

import com.freshtrack.api.product.dto.ProductRequest;
import com.freshtrack.api.product.dto.ProductResponse;
import com.freshtrack.api.product.service.IProductService;
import com.freshtrack.api.user.User;
import com.freshtrack.api.user.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final IProductService productService;
    private final IUserService userService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ProductRequest request) {
        User user = userService.getUserByEmail(authHeader.substring(7));

        Product product = new Product();
        product.setName(request.name());
        product.setQuantity(request.quantity());
        product.setUnit(request.unit());
        product.setPurchaseDate(request.purchaseDate());
        product.setExpiryDate(request.expiryDate());
        product.setCategory(request.category());
        product.setUser(user);

        Product saved = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(toResponse(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestHeader("Authorization") String authHeader) {
        User user = userService.getUserByEmail(authHeader.substring(7));
        List<ProductResponse> products = productService.getAllProductsByUser(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setQuantity(request.quantity());
        product.setUnit(request.unit());
        product.setPurchaseDate(request.purchaseDate());
        product.setExpiryDate(request.expiryDate());
        product.setCategory(request.category());

        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getUnit(),
                product.getPurchaseDate(),
                product.getExpiryDate(),
                product.getCategory()
        );
    }
}



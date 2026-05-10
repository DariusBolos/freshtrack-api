package com.freshtrack.api.scan;

import com.freshtrack.api.enums.ProductCategory;
import com.freshtrack.api.product.Product;
import com.freshtrack.api.product.dto.ProductRequest;
import com.freshtrack.api.product.service.IProductService;
import com.freshtrack.api.user.User;
import com.freshtrack.api.user.service.IUserService;
import com.freshtrack.api.scan.gemini.GeminiReceiptClient;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReceiptScanService {
    private final GeminiReceiptClient geminiReceiptClient;
    private final IProductService productService;
    private final IUserService userService;

    public ReceiptScanService(GeminiReceiptClient geminiReceiptClient,
                              IProductService productService,
                              IUserService userService) {
        this.geminiReceiptClient = geminiReceiptClient;
        this.productService = productService;
        this.userService = userService;
    }

    public List<ProductRequest> scanReceipt(MultipartFile image) {
        return geminiReceiptClient.analyzeReceipt(image);
    }

    public List<Product> confirmAndPersist(String authToken, List<ProductRequest> items) {
        User user = userService.getUserByEmail(authToken);
        return items.stream()
                .map(item -> toProduct(item, user))
                .map(productService::createProduct)
                .toList();
    }

    private Product toProduct(ProductRequest request, User user) {
        Product product = new Product();
        product.setName(request.name());
        product.setQuantity(request.quantity());
        product.setUnit(request.unit());
        product.setPurchaseDate(request.purchaseDate());
        product.setExpiryDate(request.expiryDate());
        product.setCategory(request.category());
        product.setUser(user);
        return product;
    }
}

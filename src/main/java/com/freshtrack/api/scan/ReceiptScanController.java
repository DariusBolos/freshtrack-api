package com.freshtrack.api.scan;

import com.freshtrack.api.product.Product;
import com.freshtrack.api.product.dto.ProductResponse;
import com.freshtrack.api.scan.dto.ReceiptScanConfirmRequest;
import com.freshtrack.api.scan.dto.ReceiptScanResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/scan")
public class ReceiptScanController {
    private final ReceiptScanService receiptScanService;

    public ReceiptScanController(ReceiptScanService receiptScanService) {
        this.receiptScanService = receiptScanService;
    }

    @PostMapping(value = "/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReceiptScanResponse> scanReceipt(
            @RequestPart("image") @NotNull MultipartFile image
    ) {
        ReceiptScanResponse response = new ReceiptScanResponse(receiptScanService.scanReceipt(image));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/receipt/confirm")
    public ResponseEntity<List<ProductResponse>> confirmReceipt(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReceiptScanConfirmRequest request
    ) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        List<Product> saved = receiptScanService.confirmAndPersist(token, request.items());
        List<ProductResponse> response = saved.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(response);
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

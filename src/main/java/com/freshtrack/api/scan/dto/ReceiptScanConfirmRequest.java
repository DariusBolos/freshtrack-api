package com.freshtrack.api.scan.dto;

import com.freshtrack.api.product.dto.ProductRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ReceiptScanConfirmRequest(
        @NotEmpty @Valid List<ProductRequest> items
) {}


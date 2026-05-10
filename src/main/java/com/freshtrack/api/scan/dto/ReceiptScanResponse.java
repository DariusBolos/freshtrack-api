package com.freshtrack.api.scan.dto;

import com.freshtrack.api.product.dto.ProductRequest;
import java.util.List;

public record ReceiptScanResponse(
        List<ProductRequest> items
) {}


package kr.yuns.springinitialize.product.presentation.dto;

import kr.yuns.springinitialize.product.application.dto.ProductResult;

import java.time.LocalDateTime;

public record ProductResponse(Long id, String name, String description, LocalDateTime createdAt) {
    public static ProductResponse from(ProductResult result) {
        return new ProductResponse(result.id(), result.name(), result.description(), result.createdAt());
    }
}

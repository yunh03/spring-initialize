package kr.yuns.springinitialize.product.application.dto;

import kr.yuns.springinitialize.product.domain.Product;

import java.time.LocalDateTime;

public record ProductResult(Long id, String name, String description, LocalDateTime createdAt) {
    public static ProductResult from(Product product) {
        return new ProductResult(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCreatedAt()
        );
    }
}

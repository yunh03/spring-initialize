package kr.yuns.springinitialize.product.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import kr.yuns.springinitialize.product.application.dto.ProductCreateCommand;

public record ProductCreateRequest(
        @NotBlank String name,
        @NotBlank String description
) {
    public ProductCreateCommand toCommand(String ownerEmail) {
        return new ProductCreateCommand(ownerEmail, name, description);
    }
}

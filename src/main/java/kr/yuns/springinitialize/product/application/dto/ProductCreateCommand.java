package kr.yuns.springinitialize.product.application.dto;

public record ProductCreateCommand(String ownerEmail, String name, String description) {
}

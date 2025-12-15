package kr.yuns.springinitialize.product.service;

import kr.yuns.springinitialize.common.response.GlobalResponse;
import kr.yuns.springinitialize.product.data.dto.ProductRequestDto;
import kr.yuns.springinitialize.product.data.dto.ProductResponseDto;
import kr.yuns.springinitialize.product.data.entity.Product;
import kr.yuns.springinitialize.product.data.exception.ProductNotFoundException;
import kr.yuns.springinitialize.product.data.repository.ProductRepository;
import kr.yuns.springinitialize.user.data.entity.User;
import kr.yuns.springinitialize.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final AuthService authService;

    private Product getProductEntity(Long id) {
        Optional<Product> product = productRepository.findById(id);

        if(product.isPresent()) {
            return product.get();
        } else {
            throw new ProductNotFoundException();
        }
    }

    public GlobalResponse<Void> createProduct(String email, ProductRequestDto productRequestDto) {
        User user = authService.getUserEntity(email);

        productRepository.save(
                Product.builder()
                        .name(productRequestDto.getName())
                        .description(productRequestDto.getDescription())
                        .user(user)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return GlobalResponse.ok();
    }

    public GlobalResponse<ProductResponseDto> getProduct(Long id) {
        Product product = getProductEntity(id);

        return GlobalResponse.ok(
                ProductResponseDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .createdAt(product.getCreatedAt())
                        .build()
        );
    }
}

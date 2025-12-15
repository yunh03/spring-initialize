package kr.yuns.springinitialize.product.controller;

import kr.yuns.springinitialize.common.response.GlobalResponse;
import kr.yuns.springinitialize.common.security.SecurityUtil;
import kr.yuns.springinitialize.product.data.dto.ProductRequestDto;
import kr.yuns.springinitialize.product.data.dto.ProductResponseDto;
import kr.yuns.springinitialize.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping("/")
    public GlobalResponse<Void> createProduct(@RequestBody ProductRequestDto productRequestDto) {
        return productService.createProduct(SecurityUtil.getUsername(), productRequestDto);
    }

    @GetMapping("/")
    public GlobalResponse<ProductResponseDto> getProduct(@RequestParam Long id) {
        return productService.getProduct(id);
    }
}

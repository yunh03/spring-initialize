package kr.yuns.springinitialize.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kr.yuns.springinitialize.global.response.GlobalResponse;
import kr.yuns.springinitialize.global.security.SecurityUtil;
import kr.yuns.springinitialize.product.application.ProductService;
import kr.yuns.springinitialize.product.presentation.dto.ProductCreateRequest;
import kr.yuns.springinitialize.product.presentation.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @Operation(summary = "상품 등록")
    public GlobalResponse<Void> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        productService.createProduct(request.toCommand(SecurityUtil.getUsername()));
        return GlobalResponse.ok();
    }

    @GetMapping
    @Operation(summary = "상품 조회")
    public GlobalResponse<ProductResponse> getProduct(@RequestParam Long id) {
        return GlobalResponse.ok(ProductResponse.from(productService.getProduct(id)));
    }
}

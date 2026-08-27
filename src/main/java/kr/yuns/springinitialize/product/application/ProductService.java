package kr.yuns.springinitialize.product.application;

import kr.yuns.springinitialize.product.application.dto.ProductCreateCommand;
import kr.yuns.springinitialize.product.application.dto.ProductResult;
import kr.yuns.springinitialize.product.domain.Product;
import kr.yuns.springinitialize.product.domain.ProductRepository;
import kr.yuns.springinitialize.product.domain.exception.ProductNotFoundException;
import kr.yuns.springinitialize.user.application.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final UserQueryService userQueryService;

    @Transactional
    public ProductResult createProduct(ProductCreateCommand command) {
        Long ownerId = userQueryService.getUserIdByEmail(command.ownerEmail());

        Product product = productRepository.save(
                Product.create(ownerId, command.name(), command.description())
        );

        return ProductResult.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResult getProduct(Long id) {
        return ProductResult.from(
                productRepository.findById(id)
                        .orElseThrow(ProductNotFoundException::new)
        );
    }
}

package kr.yuns.springinitialize.product.data.repository;

import kr.yuns.springinitialize.product.data.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}

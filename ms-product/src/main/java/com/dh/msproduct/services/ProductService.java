package com.dh.msproduct.services;

import com.dh.msproduct.models.Product;
import com.dh.msproduct.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductService  implements  IProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public Flux<Product> findAllProducts() {
        return productRepository.findAll();
    }
    @Override
    public Mono<Product> findProductById(String id) {
        return null;
    }
    @Override
    public Mono<Product> saveProduct(Product product) {
        return null;
    }
    @Override
    public Mono<Void> deleteProduct(String id) {
        return null;
    }
}

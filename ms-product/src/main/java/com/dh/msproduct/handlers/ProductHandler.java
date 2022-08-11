package com.dh.msproduct.handlers;

import com.dh.msproduct.models.Product;
import com.dh.msproduct.services.IProductService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProductHandler {
    private final IProductService productService;
    private final Validator validator;

    public Mono<ServerResponse> listProducts(ServerRequest request){
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAllProducts(), Product.class);
    }

    @SneakyThrows
    public Mono<ServerResponse> getProduct(ServerRequest request){
        var id= request.pathVariable("id");
        return productService.findProductById(id)
                .flatMap(product -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(product)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> saveProduct(ServerRequest request){
        var producto = request.bodyToMono(Product.class);
        return producto.flatMap(product -> {
            Errors errors = new BeanPropertyBindingResult(product, Product.class.getName());
            validator.validate(product, errors);
            if(errors.hasErrors()){
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError ->
                                String.format("El campo %s %s", fieldError.getField(),
                                        fieldError.getDefaultMessage()))
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromValue(list)));
            }
            else{
                if(product.getCreatedAt()==null) product.setCreatedAt(LocalDateTime.now());
                return   productService.saveProduct(product)
                            .flatMap(product1 ->
                                    ServerResponse.created(URI.create("/api/v1/products/".concat(product1.getIdProduct()))).contentType(MediaType.APPLICATION_JSON)
                                    .body(BodyInserters.fromValue(product1)));
            }
        });
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request){
        var id = request.pathVariable("id");
        var product = request.bodyToMono(Product.class);
        var productDB = productService.findProductById(id);

        return productDB.zipWith(product, (pdb, req) -> {
            pdb.setName(req.getName());
            pdb.setPrice(req.getPrice());
            pdb.setCreatedAt(req.getCreatedAt());
            return pdb;
        }).flatMap(product1 -> {
                return ServerResponse.created(URI.create("/api/v1/products/".concat(product1.getIdProduct()))).contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(product1)).switchIfEmpty(ServerResponse.notFound().build());
        });
    }
}

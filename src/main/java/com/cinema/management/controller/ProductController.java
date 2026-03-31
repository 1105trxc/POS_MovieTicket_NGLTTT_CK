package com.cinema.management.controller;

import com.cinema.management.model.entity.Product;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.repository.ProductRepository;
import com.cinema.management.service.IProductService;
import com.cinema.management.service.impl.AuditLogServiceImpl;
import com.cinema.management.service.impl.ProductServiceImpl;
import java.util.List;

public class ProductController {
    private final IProductService productService;

    public ProductController() {
        // Nối DI thủ công
        this.productService = new ProductServiceImpl(
                new ProductRepository(),
                new AuditLogServiceImpl(new AuditLogRepository()));
    }

    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    public void addProduct(Product product) {
        productService.createProduct(product);
    }

    public void updateProduct(Product product) {
        productService.updateProduct(product);
    }

    public void deleteProduct(String id) {
        productService.deleteProduct(id);
    }
}

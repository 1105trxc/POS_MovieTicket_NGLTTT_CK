package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Product;
import com.cinema.management.repository.ProductRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.IProductService;
import java.util.List;

public class ProductServiceImpl implements IProductService {
    private final ProductRepository productRepo;
    private final IAuditLogService auditLogService;

    public ProductServiceImpl(ProductRepository productRepo, IAuditLogService auditLogService) {
        this.productRepo = productRepo;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public Product getProductById(String id) {
        return productRepo.findById(id).orElse(null);
    }

    @Override
    public void createProduct(Product product) {
        productRepo.save(product);
        auditLogService.logAction("CREATE", "Product", "ID: " + product.getProductId(), "None",
                product.getProductName());
    }

    @Override
    public void updateProduct(Product product) {
        // Lấy dữ liệu cũ trước khi cập nhật
        Product old = productRepo.findById(product.getProductId()).orElse(null);
        String oldPrice = old != null && old.getCurrentPrice() != null ? old.getCurrentPrice().toPlainString() : "N/A";

        productRepo.update(product);
        auditLogService.logAction("UPDATE", "Product", "Sửa giá trị",
                oldPrice, product.getCurrentPrice().toPlainString());
    }

    @Override
    public void deleteProduct(String id) {
        productRepo.delete(id);
        auditLogService.logAction("DELETE", "Product", "Xóa sản phẩm", id, "None");
    }
}

package net.jingle.bells.data.service;

import net.jingle.bells.data.entity.Product;
import net.jingle.bells.data.entity.ProductCategory;
import net.jingle.bells.data.repository.ProductRepository;
import net.jingle.bells.data.specification.ProductSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        if (product == null) {
            System.err.println("Product is null.");
            return null;
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    public List<Product> findProductsByFilter(String nameFilter, ProductCategory categoryFilter) {
        Specification<Product> spec = ProductSpecification.createSpecification(nameFilter, categoryFilter);
        return productRepository.findAll(spec);
    }
}
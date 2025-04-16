package net.jingle.bells.data.service;

import net.jingle.bells.data.entity.ProductCategory;
import net.jingle.bells.data.repository.ProductCategoryRepository;
import org.springframework.stereotype.Service; // Merkitään Spring-palveluksi
import java.util.List;
import java.util.Optional;

@Service
public class ProductCategoryService {

    private final ProductCategoryRepository repository;

    public ProductCategoryService(ProductCategoryRepository repository) {
        this.repository = repository;
    }

    public List<ProductCategory> findAllCategories() {
        return repository.findAll();
    }

    public Optional<ProductCategory> findById(Long id) {
        return repository.findById(id);
    }

    public ProductCategory saveCategory(ProductCategory category) {
        if (category == null) {
            System.err.println("Category is null. Are you sure you have connected your form to the application?");
            return null;
        }
        return repository.save(category);
    }

    public void deleteCategory(Long id) {
        repository.deleteById(id);
    }
}
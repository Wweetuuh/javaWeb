package net.jingle.bells.data.specification;

import net.jingle.bells.data.entity.Product;
import net.jingle.bells.data.entity.ProductCategory;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> createSpecification(String nameFilter, ProductCategory categoryFilter) {
        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (nameFilter != null && !nameFilter.trim().isEmpty()) {
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + nameFilter.toLowerCase().trim() + "%"
                );
                predicates.add(namePredicate);
            }

            if (categoryFilter != null) {

                Predicate categoryPredicate = criteriaBuilder.equal(
                        root.get("productCategory"),
                        categoryFilter
                );
                predicates.add(categoryPredicate);
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
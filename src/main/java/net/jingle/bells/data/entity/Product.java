package net.jingle.bells.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tuotteen nimi on pakollinen")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Hinta on pakollinen")
    @Min(value = 0, message = "Hinnan tulee olla nolla tai suurempi")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Varastosaldo on pakollinen")
    @Min(value = 0, message = "Varastosaldo ei voi olla negatiivinen")
    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private boolean available = true;

    @NotNull(message = "Kategoria on pakollinen")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_category_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductCategory productCategory;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductDetail productDetail;

    public void setProductDetail(ProductDetail detail) {
        if (detail != null) {
            detail.setProduct(this);
        }
        this.productDetail = detail;
    }
}
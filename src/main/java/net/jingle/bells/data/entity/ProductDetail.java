package net.jingle.bells.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "product_details")
@Data
@NoArgsConstructor
public class ProductDetail {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    private String warrantyInfo;
    private String manufacturerInfo;

    public ProductDetail(Product product) {
        this.product = product;
        if (product != null && product.getId() != null) {
            this.id = product.getId();
        }
    }
    public void setProduct(Product product) {
        this.product = product;
        if (product != null && product.getId() != null) {
            this.id = product.getId();
        } else {
            this.id = null;
        }
    }
}

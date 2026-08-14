package com.souflow.models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(name = "id")
    private String code;

    @Column(name = "name_vn")
    private String nameVn;

    @Column(name = "name_eng")
    private String nameEng;

    @Column(name = "description_vn")
    private String descriptionVn;

    @Column(name = "description_eng")
    private String descriptionEng;

    private BigDecimal price;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    private Boolean available;

    private Integer quantity;

    private Boolean customised;

    private Long sales;
    
    @Column(name = "del_if")
    private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "category_pk")
    private Category category;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductImage> productImages = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @org.hibernate.annotations.Where(clause = "del_if = 0")
    private List<Comment> comments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "products_discounts",
        joinColumns = @JoinColumn(name = "product_pk"),
        inverseJoinColumns = @JoinColumn(name = "discount_pk")
    )
    private List<Discount> discounts = new ArrayList<>();
}

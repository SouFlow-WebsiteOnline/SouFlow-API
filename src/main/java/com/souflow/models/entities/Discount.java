package com.souflow.models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "discounts")
public class Discount {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;
	
	@Column(name = "id")
	private	String code;
	
	private	BigDecimal percentage;

	@Column(name = "min_order_amount")
	private BigDecimal minOrderAmount;
	
	@Column(name = "usage_limit")
	private Integer usageLimit;
	
	@Column(name = "current_usage")
	private Integer currentUsage;
	
	@Column(name = "description_vn")
	private String descriptionVn;
	
	@Column(name = "description_eng")
	private String descriptionEng;
	
	@Column(name = "created_date")
	private LocalDateTime createdDate;
	
	@Column(name = "expired_date")
	private LocalDateTime expiredDate;
	
	private Boolean expired;
	
	@Column(name = "del_if")
	private Boolean deleted;
	
	@ManyToMany
    @JoinTable(
        name = "products_discounts",
        joinColumns = @JoinColumn(name = "discount_pk"),
        inverseJoinColumns = @JoinColumn(name = "product_pk")
    )
    private List<Product> products = new ArrayList<>();
}

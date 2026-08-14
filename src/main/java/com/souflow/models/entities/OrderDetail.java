package com.souflow.models.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="orders_details")
public class OrderDetail {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @Column(name = "product_name_vn")
    private String nameVn;

    @Column(name = "product_name_eng")
    private String nameEng;

    @Column(name = "product_price")
    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "order_pk") 
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_pk")
	public Product product;
}

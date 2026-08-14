package com.souflow.models.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.souflow.models.enums.OrderStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name="Orders")
public class Order {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pk;
	    
	@Column(name ="id")
	private String code;
	
	private String fullname;

	@Column(name = "phone_number")
    private String phone;
    
    private String address;
    
	@Setter(AccessLevel.NONE)
	private BigDecimal total;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "created_date")
	private LocalDateTime createdDate;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "expired_date")
	private LocalDateTime expiredDate;

	private Boolean expired;

	@Enumerated(EnumType.STRING)
    @Column(insertable = false)
	private OrderStatus status;
	
	@Column(name = "shipping_fee")
	private BigDecimal shippingFee;
	
	@Column(name = "payment_method")
	private String paymentMethod;
	
	@Column(name = "discount_code")
	private String discountCode;
	
	@Column(name = "discount_amount")
	private BigDecimal discountAmount;

	@Column(name = "del_if")
	private Boolean deleted;

    @ManyToOne
    @JoinColumn(name = "account_pk")
    private Account account;
    
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;

	public void calTotal() {
		BigDecimal temp = BigDecimal.ZERO;
		if (orderDetails != null) {
			for (OrderDetail od : orderDetails) {
				temp = temp.add(od.getSubtotal());
			}
		}
		if (shippingFee != null) {
			temp = temp.add(shippingFee);
		}
		if (discountAmount != null) {
			temp = temp.subtract(discountAmount);
		}
		// Ensure total doesn't go below 0
		if (temp.compareTo(BigDecimal.ZERO) < 0) {
			temp = BigDecimal.ZERO;
		}
		total = temp;
	}
	
	/*
	 the cascate type:
		ALL
		PERSIST: create children when parent get create
		MERGE: update children when parent get update
		REMOVE: remove children when parent get remove
		REFRESH: refresh children when parent get refrest
		DETATCH: detach parent and children from persistence context
	 */
}

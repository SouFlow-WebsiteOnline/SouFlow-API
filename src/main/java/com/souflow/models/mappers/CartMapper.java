package com.souflow.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.souflow.models.entities.Account;
import com.souflow.models.entities.Cart;
import com.souflow.models.entities.Item;
import com.souflow.models.repositories.CartRepository;
import com.souflow.models.requests.CartRequest;
import com.souflow.models.responses.CartResponse;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring", uses = {ItemMapper.class})
public abstract class CartMapper {

	@Autowired
	private CartRepository cartRepo;

	@Mapping(target = "code", 			ignore = true)
	@Mapping(target = "createdDate", 	ignore = true)
	@Mapping(target = "expiredDate", 	ignore = true)
	@Mapping(target = "expired", 		ignore = true)
	@Mapping(target = "total", 			ignore = true)
	@Mapping(target = "account", 		ignore = true)
	@Mapping(target = "deleted", 		ignore = true)
	@Mapping(target = "items", 			source = "request.itemRequests")
	public abstract Cart toEntity(CartRequest request);
	
	@Mapping(target = "createdDate", 	source = "createdDate", dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "total", 			source = "total", numberFormat = "#.##")
	@Mapping(target = "username", 		source = "account.username")
	@Mapping(target = "fullname", 		source = "account.fullname")
	@Mapping(target = "accountPk", 		source = "account.pk")
	@Mapping(target = "itemResponses", 	source = "items")
	public abstract CartResponse toResponse(Cart cart);
	
	public abstract List<CartResponse> toResponseList(List<Cart> carts);
	
	@AfterMapping
	protected void afterToEntity(CartRequest request, @MappingTarget Cart cart) {
		Long pk = cart.getPk();
		if (pk != null) {
			Cart oldCart = cartRepo.findById(pk).orElseThrow(() -> new EntityNotFoundException("Order not found with pk: "+ pk));
			cart.setCode(oldCart.getCode());
			cart.setCreatedDate(oldCart.getCreatedDate());
			cart.setExpiredDate(oldCart.getExpiredDate());
			cart.setExpired(oldCart.getExpired());
			cart.setDeleted(oldCart.getDeleted());
			cart.setAccount(oldCart.getAccount());
		} else {
			cart.setCode("CA-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
			cart.setCreatedDate(LocalDateTime.now());
			cart.setExpiredDate(LocalDateTime.now().plusWeeks(2));
			cart.setExpired(false);
			if (request.getAccountPk() != null) {
				Account account = new Account();
				account.setPk(request.getAccountPk());
				cart.setAccount(account);
			} else {
				cart.setAccount(null);
			}
			cart.setDeleted(false);
		}
		
		if (cart.getItems() != null) {
			for (Item it : cart.getItems()) {
				it.setCart(cart);
			}
		}
		cart.calTotal();
	}
}

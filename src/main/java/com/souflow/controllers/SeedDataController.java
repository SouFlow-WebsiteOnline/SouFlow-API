package com.souflow.controllers;

import com.souflow.models.entities.Product;
import com.souflow.models.entities.Order;
import com.souflow.models.entities.OrderDetail;
import com.souflow.models.enums.OrderStatus;
import com.souflow.models.repositories.OrderRepository;
import com.souflow.models.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
public class SeedDataController {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private OrderRepository orderRepo;

    @GetMapping("/seed-fake-orders")
    public String seedFakeOrders() {
        List<Product> products = productRepo.findAll().stream().filter(p -> p.getAvailable() != null && p.getAvailable() && (p.getDeleted() == null || !p.getDeleted())).toList();
        if (products.isEmpty()) {
            return "No available products found to seed orders.";
        }

        Random rand = new Random();
        OrderStatus[] statuses = OrderStatus.values();

        int generated = 0;
        for (int i = 0; i < 200; i++) {
            Order order = new Order();
            order.setCode("O-FAKE" + i);
            order.setFullname("Fake Customer " + i);
            order.setPhone("090" + (1000000 + rand.nextInt(8999999)));
            order.setAddress("Fake Address " + i);
            order.setPaymentMethod(rand.nextBoolean() ? "COD" : "STORE");
            order.setStatus(statuses[rand.nextInt(statuses.length)]);
            order.setDeleted(false);
            order.setExpired(false);
            
            // Random date in the last 12 months
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime randomDate = now.minusDays(rand.nextInt(365));
            order.setCreatedDate(randomDate);
            order.setExpiredDate(randomDate.plusWeeks(2));

            // Random 1 to 3 products
            int numProducts = 1 + rand.nextInt(3);
            List<OrderDetail> details = new ArrayList<>();
            BigDecimal total = BigDecimal.ZERO;
            for (int j = 0; j < numProducts; j++) {
                Product p = products.get(rand.nextInt(products.size()));
                OrderDetail detail = new OrderDetail();
                detail.setOrder(order);
                detail.setProduct(p);
                detail.setNameVn(p.getNameVn());
                detail.setNameEng(p.getNameEng());
                detail.setPrice(p.getPrice());
                int qty = 1 + rand.nextInt(3);
                detail.setQuantity(qty);
                BigDecimal sub = p.getPrice().multiply(BigDecimal.valueOf(qty));
                detail.setSubtotal(sub);
                details.add(detail);
                total = total.add(sub);
            }
            order.setOrderDetails(details);
            order.setShippingFee(BigDecimal.valueOf(30000));
            order.calTotal();
            
            orderRepo.save(order);
            generated++;
        }

        return "Successfully seeded " + generated + " fake orders spanning the last 12 months!";
    }
}

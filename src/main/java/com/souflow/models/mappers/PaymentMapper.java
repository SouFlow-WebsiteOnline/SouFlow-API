package com.souflow.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.souflow.models.entities.Order;
import com.souflow.models.entities.Payment;
import com.souflow.models.requests.PaymentRequest;
import com.souflow.models.responses.PaymentResponse;

@Mapper(componentModel = "spring", uses = {OrderMapper.class})
public abstract class PaymentMapper {


    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "order", ignore = true)
    public abstract Payment toEntity(PaymentRequest request);

    @Mapping(target = "orderResponse", source = "order")
    public abstract PaymentResponse toResponse(Payment payment);

    public abstract List<PaymentResponse> toResponseList(List<Payment> payments);

    @AfterMapping
    protected void afterToEntity(PaymentRequest request, @MappingTarget Payment payment) {
        payment.setPaymentDate(LocalDateTime.now());
        Order order = new Order();
        order.setPk(request.getPk());
        payment.setOrder(order);
    }
}

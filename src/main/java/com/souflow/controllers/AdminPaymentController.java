package com.souflow.controllers;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.requests.PaymentRequest;
import com.souflow.models.responses.PaymentResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;


    @PostMapping("/payment")
    PaymentResponse save(@RequestBody PaymentRequest request) {
        return paymentService.save(request);
    }   

    public void processSepayWebhook(String sepaySignature, String sepayTimestamp, byte[] rawPayloadBytes) {
        paymentService.processSepayWebhook(sepaySignature, sepayTimestamp, rawPayloadBytes);
    }

}

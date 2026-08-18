package com.souflow.models.services;

import com.souflow.models.requests.PaymentRequest;
import com.souflow.models.responses.PaymentResponse;
import com.souflow.models.requests.SepayWebhookRequest;

public interface PaymentService {

    PaymentResponse save(PaymentRequest request);
    void processSepayWebhook(String signature, String timestamp, byte[] rawPayloadBytes);
}

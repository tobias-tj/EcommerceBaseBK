package com.example.demo.service;

import com.example.demo.model.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripeService {
    @Value("${stripe.secret-key}")
    private String secretKey;

    public String createPaymentIntent( Long amount, String currency, String paymentMethodId, String phoneNumber, User user ) throws StripeException {
        Stripe.apiKey = secretKey;

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                .setReturnUrl("http://localhost:5175/confirmCheckout")
                .putMetadata("user_id", String.valueOf(user.getId()))
                .putMetadata("user_email", user.getEmail())
                .putMetadata("user_phone", phoneNumber)
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.getId();
    }


}

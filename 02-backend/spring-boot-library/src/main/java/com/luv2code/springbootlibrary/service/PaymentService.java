package com.luv2code.springbootlibrary.service;

import com.luv2code.springbootlibrary.dao.PaymentRepository;
import com.luv2code.springbootlibrary.entity.Payment;
import com.luv2code.springbootlibrary.requestmodels.PaymentInfoRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RequestOptions stripeRequestOptions;

    public PaymentService(
            PaymentRepository paymentRepository,
            @Value("${stripe.key.secret}") String secretKey) {
        this.paymentRepository = paymentRepository;
        this.stripeRequestOptions = RequestOptions.builder().setApiKey(secretKey).build();
    }

    public PaymentIntent createPaymentIntent(PaymentInfoRequest paymentInfoRequest) throws StripeException {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", paymentInfoRequest.getAmount());
        params.put("currency", paymentInfoRequest.getCurrency());
        // Stripe no longer accepts "payment_method_types"; methods are managed in the Stripe Dashboard.
        // allow_redirects=never keeps the card-only confirmCardPayment flow working without a return_url.
        params.put("automatic_payment_methods", Map.of("enabled", true, "allow_redirects", "never"));
        return PaymentIntent.create(params, stripeRequestOptions);
    }

    public void stripePayment(String userEmail) {
        Payment payment = paymentRepository.findByUserEmail(userEmail);

        if (payment == null) {
            throw new EntityNotFoundException("Payment information is missing");
        }
        payment.setAmount(0.0);
        paymentRepository.save(payment);
    }
}

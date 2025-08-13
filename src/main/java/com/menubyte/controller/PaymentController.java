package com.menubyte.controller;
import com.menubyte.dto.PaymentRequest;
import com.menubyte.entity.BusinessMaster;
import com.menubyte.enums.SubscriptionType;
import com.menubyte.repository.BusinessMasterRepository;
import com.razorpay.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    BusinessMasterRepository businessMasterRepository;
    @PostMapping("/create-order")
    public String createOrder(@RequestBody PaymentRequest request) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient("rzp_test_jI5D0vXwBG7OpO", "wwlqWH1r0KWz0p3MC0p9ncwa");

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount() * 100); // amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
        orderRequest.put("payment_capture", 1);

        Order order = razorpay.orders.create(orderRequest);

        return order.toString();
    }
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> data) {
        String orderId = data.get("razorpay_order_id");
        String paymentId = data.get("razorpay_payment_id");
        String signature = data.get("razorpay_signature");

        if (orderId == null || paymentId == null || signature == null) {
            return new ResponseEntity<>(Map.of("status", "failed", "message", "Missing required payment details."), HttpStatus.BAD_REQUEST);
        }

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        try {
            // Step 1: Verify the payment signature using the key secret
            boolean isVerified = Utils.verifyPaymentSignature(options, "wwlqWH1r0KWz0p3MC0p9ncwa");

            if (isVerified) {
                // Step 2: Extract and use subscription details
                String businessId = data.get("businessId");
                String planType = data.get("planType");
                String tenureInMonths = data.get("tenureInMonths");

                // Perform your business logic here!
                // Example: Call a service to update the database
                // subscriptionService.activateSubscription(businessId, planType, tenureInMonths, paymentId);
                BusinessMaster businessOptional = businessMasterRepository.findByBusinessId(Long.valueOf(businessId));
                businessOptional.setSubscriptionType(SubscriptionType.valueOf(planType));
                businessOptional.setEndDate(businessOptional.getEndDate().plusMonths(Long.valueOf(tenureInMonths)));
                businessMasterRepository.save(businessOptional);
                System.out.println("Payment verified successfully for orderId: {}"+orderId);
                return new ResponseEntity<>(Map.of("status", "success"), HttpStatus.OK);
            } else {
                System.out.println("Payment signature verification failed for orderId: {}"+orderId);
                return new ResponseEntity<>(Map.of("status", "failed", "message", "Signature verification failed."), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            System.out.println("An unexpected error occurred during payment verification for orderId: {}" + orderId);
            return new ResponseEntity<>(Map.of("status", "failed", "message", "Internal server error."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
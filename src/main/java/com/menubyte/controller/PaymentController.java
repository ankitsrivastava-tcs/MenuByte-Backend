package com.menubyte.controller;
import com.menubyte.dto.PaymentRequest;
import com.menubyte.dto.PaymentVerificationRequest;
import com.menubyte.entity.BusinessMaster;
import com.menubyte.entity.OrderItem;
import com.menubyte.enums.OrderStatus;
import com.menubyte.enums.PaymentStatus;
import com.menubyte.enums.SubscriptionType;
import com.menubyte.repository.BusinessMasterRepository;
import com.menubyte.repository.OrderRepository;
import com.razorpay.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    BusinessMasterRepository businessMasterRepository;
    @Autowired
    OrderRepository orderRepository;
    @PostMapping("/create-order")
    public String createOrder(@RequestBody PaymentRequest request) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient("rzp_test_jI5D0vXwBG7OpO", "wwlqWH1r0KWz0p3MC0p9ncwa");

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount() * 1); // amount in paise
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
                businessOptional.setAmountPaid(Double.parseDouble(data.get("amountPaid")));
                businessMasterRepository.save(businessOptional);
                log.info("subscription_payment_verified razorpayOrderId={}", orderId);
                return new ResponseEntity<>(Map.of("status", "success"), HttpStatus.OK);
            } else {
                log.warn("subscription_payment_signature_invalid razorpayOrderId={}", orderId);
                return new ResponseEntity<>(Map.of("status", "failed", "message", "Signature verification failed."), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("subscription_payment_verification_failed razorpayOrderId={}", orderId, e);
            throw new RuntimeException("Subscription payment verification failed.", e);
        }
    }

    @PostMapping("/verifyPayment")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        // --- 1. HANDLE CASH / PAY AT COUNTER DIRECTLY (BYPASS RAZORPAY SIGNATURE CHECK) ---
        if (request.getPaymentDetails() != null &&
                ("CASH".equalsIgnoreCase(request.getPaymentDetails().getPaymentMethod()) ||
                        "PAY_AT_COUNTER".equalsIgnoreCase(request.getPaymentDetails().getPaymentMethod()))) {

            // Inside PaymentController.java under the CASH / PAY_AT_COUNTER block
            // Inside the CASH / PAY_AT_COUNTER block in PaymentController.java
            try {
                com.menubyte.entity.Order newOrder = new com.menubyte.entity.Order();

                String customOfflineId = request.getRazorpay_order_id() != null ?
                        request.getRazorpay_order_id() : "OFFLINE_ORD_" + System.currentTimeMillis();
                String customPaymentId = request.getRazorpay_payment_id() != null ?
                        request.getRazorpay_payment_id() : "OFFLINE_PAY_" + System.currentTimeMillis();
                newOrder.setTableNumber(request.getTableNumber());
                newOrder.setRazorpayOrderId(customOfflineId);
                newOrder.setRazorpayPaymentId(customPaymentId);
                newOrder.setBusinessId(request.getBusinessId());
                newOrder.setUserId(request.getUserId());
                newOrder.setTotalAmount(new BigDecimal(request.getPaymentDetails().getAmount()));
                newOrder.setOrderNote(request.getOrderNote());

                // Set all statuses and modes to prevent validation blocks
                newOrder.setPaymentStatus(PaymentStatus.PENDING);
                newOrder.setOrderStatus(com.menubyte.enums.OrderStatus.PENDING);

                // --- SET THE ENUM PAYMENT MODE EXPLICITLY HERE ---
                if ("PAY_AT_COUNTER".equalsIgnoreCase(request.getPaymentDetails().getPaymentMethod())) {
                    newOrder.setPaymentMode(com.menubyte.enums.PaymentMode.PAY_AT_COUNTER);
                } else {
                    newOrder.setPaymentMode(com.menubyte.enums.PaymentMode.CASH);
                }

                // Map the items from the request payload to OrderItem entities
                List<OrderItem> orderItemsList = request.getOrderItems().stream()
                        .map(itemMap -> {
                            OrderItem item = new OrderItem();
                            item.setOrder(newOrder);
                            item.setItemId(Long.valueOf(itemMap.get("itemId").toString()));
                            item.setItemName(itemMap.get("itemName").toString());
                            item.setVariantName(itemMap.get("variantName").toString());
                            item.setQuantity(Integer.valueOf(itemMap.get("quantity").toString()));
                            item.setPrice(new BigDecimal(itemMap.get("price").toString()));
                            return item;
                        })
                        .collect(Collectors.toList());

                newOrder.setOrderItems(orderItemsList);

                // Save parent order record
                com.menubyte.entity.Order savedOrder = orderRepository.save(newOrder);

                log.info("offline_order_created orderId={} businessId={}", savedOrder.getId(), request.getBusinessId());
                return new ResponseEntity<>(Map.of(
                        "status", "success",
                        "message", "Cash order placed successfully",
                        "orderId", savedOrder.getId()
                ), HttpStatus.OK);

            } catch (Exception e) {
                log.error("offline_order_creation_failed businessId={}", request.getBusinessId(), e);
                throw new RuntimeException("Offline order creation failed.", e);
            }
        }

        // --- 2. EXISTING ONLINE PAYMENT FLOW (RAZORPAY VERIFICATION SYSTEM) ---
        String orderId = request.getRazorpay_order_id();
        String paymentId = request.getRazorpay_payment_id();
        String signature = request.getRazorpay_signature();

        if (orderId == null || paymentId == null || signature == null) {
            return new ResponseEntity<>(Map.of("status", "failed", "message", "Missing required payment details."), HttpStatus.BAD_REQUEST);
        }

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        try {
            boolean isVerified = Utils.verifyPaymentSignature(options, "wwlqWH1r0KWz0p3MC0p9ncwa");

            if (isVerified) {
                com.menubyte.entity.Order newOrder = new com.menubyte.entity.Order();
                newOrder.setRazorpayOrderId(orderId);
                newOrder.setRazorpayPaymentId(paymentId);
                newOrder.setBusinessId(request.getBusinessId());
                newOrder.setUserId(request.getUserId());
                newOrder.setTotalAmount(new BigDecimal(request.getPaymentDetails().getAmount()));
                newOrder.setOrderNote(request.getOrderNote());

                // Paid immediately on gate check success
                newOrder.setPaymentStatus(PaymentStatus.PAID);
                newOrder.setOrderStatus(com.menubyte.enums.OrderStatus.PENDING);

                List<OrderItem> orderItemss = request.getOrderItems().stream()
                        .map(itemMap -> {
                            OrderItem item = new OrderItem();
                            item.setOrder(newOrder);
                            item.setItemId(Long.valueOf(itemMap.get("itemId").toString()));
                            item.setItemName(itemMap.get("itemName").toString());
                            item.setVariantName(itemMap.get("variantName").toString());
                            item.setQuantity(Integer.valueOf(itemMap.get("quantity").toString()));
                            item.setPrice(new BigDecimal(itemMap.get("price").toString()));
                            return item;
                        })
                        .collect(Collectors.toList());

                newOrder.setOrderItems(orderItemss);
                orderRepository.save(newOrder);


                log.info("order_payment_verified razorpayOrderId={} businessId={}", orderId, request.getBusinessId());
                return new ResponseEntity<>(Map.of("status", "success", "message", "Payment verified successfully",  "orderId", newOrder.getId()), HttpStatus.OK);
            } else {
                log.warn("order_payment_signature_invalid razorpayOrderId={}", orderId);
                return new ResponseEntity<>(Map.of("status", "failed", "message", "Signature verification failed."), HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.error("order_payment_verification_failed razorpayOrderId={} businessId={}", orderId, request.getBusinessId(), e);
            throw new RuntimeException("Order payment verification failed.", e);
        }
    }

}

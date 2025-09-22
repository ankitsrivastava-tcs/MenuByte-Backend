package com.menubyte.controller;

import com.menubyte.entity.Order;
import com.menubyte.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<Order>> getOrdersByBusinessId(@PathVariable Long businessId) {
        List<Order> orders = orderService.getOrdersByBusinessId(businessId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/business/{businessId}/today")
    public ResponseEntity<List<Order>> getTodaysOrdersByBusinessId(@PathVariable Long businessId) {
        List<Order> orders = orderService.getTodaysOrdersByBusinessId(businessId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/business/{businessId}/today-sale")
    public ResponseEntity<Double> getTodaysSale(@PathVariable Long businessId) {
        double totalSale = orderService.calculateTodaysSale(businessId);
        return ResponseEntity.ok(totalSale);
    }

    /**
     * Endpoint to find a single order by its ID.
     *
     * @param orderId The ID of the order to search for.
     * @return A ResponseEntity with the order if found, or a 404 Not Found status.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * New endpoint to generate and download a PDF sales report for a specific date range.
     *
     * @param businessId The ID of the business.
     * @param startDate  The start date of the report range (YYYY-MM-DD).
     * @param endDate    The end date of the report range (YYYY-MM-DD).
     * @return A ResponseEntity containing the PDF file as a byte array.
     */
    @GetMapping("/business/{businessId}/report")
    public ResponseEntity<byte[]> generateSalesReport(@PathVariable Long businessId,
                                                      @RequestParam("startDate") String startDate,
                                                      @RequestParam("endDate") String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // Delegate the PDF generation to the service layer
            byte[] pdfBytes = orderService.generateSalesReport(businessId, start, end);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = String.format("sales_report_%s_to_%s.pdf", startDate, endDate);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
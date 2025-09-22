package com.menubyte.service;

import com.menubyte.dto.TopSellingItemDTO;
import com.menubyte.entity.Order;
import com.menubyte.repository.OrderRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getOrdersByBusinessId(Long businessId) {
        return orderRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public List<Order> getTodaysOrdersByBusinessId(Long businessId) {
        LocalDate today = LocalDate.now();
        return orderRepository.findByBusinessIdAndCreatedAtAfterOrderByCreatedAtDesc(businessId, today.atStartOfDay());
    }

    public double calculateTodaysSale(Long businessId) {
        return getTodaysOrdersByBusinessId(businessId).stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    /**
     * Generates a PDF sales report for a specific business within a given date range.
     *
     * @param businessId The ID of the business.
     * @param startDate  The start date of the report range.
     * @param endDate    The end date of the report range.
     * @return A byte array containing the PDF report.
     */
    public byte[] generateSalesReport(Long businessId, LocalDate startDate, LocalDate endDate) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Report Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Sales Report", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Date Range
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            Paragraph dateRange = new Paragraph(
                    "From: " + startDate.format(formatter) + " To: " + endDate.format(formatter),
                    dateFont
            );
            dateRange.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(dateRange);
            document.add(new Paragraph(" "));

            // Fetch data from the repository
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            double totalSales = orderRepository.findTotalSalesByBusinessAndPeriod(businessId, startDateTime, endDateTime);
            int totalOrders = orderRepository.findTotalOrdersByBusinessAndPeriod(businessId, startDateTime, endDateTime);
            List<TopSellingItemDTO> topSellingItems = orderRepository.findTopSellingItemsByBusinessAndPeriod(businessId, startDateTime, endDateTime);
            List<Object[]> dailySales = orderRepository.findDailySalesByBusinessAndPeriod(businessId, startDateTime, endDateTime);

            // Summary Section
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            document.add(new Paragraph("Summary", summaryFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Total Sales: ₹" + String.format("%.2f", totalSales)));
            document.add(new Paragraph("Total Orders: " + totalOrders));
            document.add(new Paragraph(" "));

            // Top Selling Items Section
            if (!topSellingItems.isEmpty()) {
                document.add(new Paragraph("Top Selling Items", summaryFont));
                document.add(new Paragraph(" "));
                for (int i = 0; i < Math.min(topSellingItems.size(), 5); i++) {
                    TopSellingItemDTO item = topSellingItems.get(i);
                    document.add(new Paragraph(
                            (i + 1) + ". " + item.getItemName() + " (" + item.getTotalSold()+ " units)"
                    ));
                }
                document.add(new Paragraph(" "));
            }

            if (!dailySales.isEmpty()) {
                document.add(new Paragraph("Daily Sales", summaryFont));
                document.add(new Paragraph(" "));
                for (Object[] dailySale : dailySales) {
                    // Fix 1: Cast java.sql.Date to LocalDate
                    java.sql.Date sqlDate = (java.sql.Date) dailySale[0];
                    LocalDate date = sqlDate.toLocalDate();

                    // Fix 2: Cast BigDecimal to Double
                    BigDecimal bigDecimalAmount = (BigDecimal) dailySale[1];
                    Double amount = bigDecimalAmount.doubleValue();

                    document.add(new Paragraph(
                            "Date: " + date.format(formatter) + " - Sales: ₹" + String.format("%.2f", amount)
                    ));
                }
                document.add(new Paragraph(" "));
            }

            // Footer
            document.add(new Paragraph("--- End of Report ---", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10)));
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF sales report.", e);
        }
    }
}
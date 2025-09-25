package com.menubyte.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
import com.menubyte.dto.TopSellingItemDTO;
import com.menubyte.entity.Order;
import com.menubyte.repository.OrderRepository;
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
import java.util.Comparator;
import java.util.Map;
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
     * Generates a PDF sales report with a detailed text-based analysis.
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

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subHeadingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Report Title
            Paragraph title = new Paragraph("Business Sales Analysis Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(new Chunk("\n")));

            // Date Range
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            Paragraph dateRange = new Paragraph(
                    "Analysis Period: " + startDate.format(formatter) + " to " + endDate.format(formatter),
                    normalFont
            );
            dateRange.setAlignment(Element.ALIGN_CENTER);
            document.add(dateRange);
            document.add(new Paragraph(new Chunk("\n\n")));

            // Fetch data
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            double totalSales = orderRepository.findTotalSalesByBusinessAndPeriod(businessId, startDateTime, endDateTime);
            int totalOrders = orderRepository.findTotalOrdersByBusinessAndPeriod(businessId, startDateTime, endDateTime);
            List<TopSellingItemDTO> topSellingItems = orderRepository.findTopSellingItemsByBusinessAndPeriod(businessId, startDateTime, endDateTime);

            // --- Sales Performance Analysis (Text) ---
            document.add(new Paragraph("Sales Performance Analysis", headingFont));
            document.add(new Paragraph(new Chunk("\n")));

            String summaryAnalysis = "This report provides a detailed breakdown of your sales performance during the specified period. Overall, your business generated a total of " +
                    totalOrders + " orders, amounting to a total sales of ₹" + String.format("%.2f", totalSales) + ".";
            document.add(new Paragraph(summaryAnalysis, normalFont));
            document.add(new Paragraph(new Chunk("\n")));

            // --- Daily Sales Trend Analysis ---
            List<Object[]> dailySales = orderRepository.findDailySalesByBusinessAndPeriod(businessId, startDateTime, endDateTime);
            if (!dailySales.isEmpty()) {
                document.add(new Paragraph("Daily Sales Trend Analysis", headingFont));
                document.add(new Paragraph(new Chunk("\n")));

                double totalDailySales = 0;
                double maxSales = 0;
                String maxSalesDate = "";
                double minSales = Double.MAX_VALUE;
                String minSalesDate = "";

                for (Object[] dailySale : dailySales) {
                    BigDecimal bigDecimalAmount = (BigDecimal) dailySale[1];
                    double amount = bigDecimalAmount.doubleValue();
                    totalDailySales += amount;

                    if (amount > maxSales) {
                        maxSales = amount;
                        maxSalesDate = ((java.sql.Date) dailySale[0]).toLocalDate().format(formatter);
                    }
                    if (amount < minSales) {
                        minSales = amount;
                        minSalesDate = ((java.sql.Date) dailySale[0]).toLocalDate().format(formatter);
                    }
                }

                double averageDailySales = totalDailySales / dailySales.size();

                String trendAnalysis = "Your average daily sales for this period were ₹" + String.format("%.2f", averageDailySales) + "." +
                        " The highest sales day was " + maxSalesDate + " with a total of ₹" + String.format("%.2f", maxSales) + "." +
                        " The lowest sales day was " + minSalesDate + " with sales of ₹" + String.format("%.2f", minSales) + "." +
                        " These trends can help you understand your peak performance days.";
                document.add(new Paragraph(trendAnalysis, normalFont));
                document.add(new Paragraph(new Chunk("\n")));
            }

            // --- Top Selling Items Analysis (Text) ---
            if (!topSellingItems.isEmpty()) {
                document.add(new Paragraph("Top Selling Items", headingFont));
                document.add(new Paragraph(new Chunk("\n")));

                TopSellingItemDTO topItem = topSellingItems.get(0);
                Paragraph topItemText = new Paragraph(
                        "The best-selling item was " + topItem.getItemName() + " with a total of " +
                                topItem.getTotalSold() + " units sold. This item is a key driver of your revenue.",
                        normalFont);
                document.add(topItemText);
                document.add(new Paragraph(new Chunk("\n")));

                document.add(new Paragraph("Here are your top 5 best-selling items:", subHeadingFont));
                document.add(new Paragraph(new Chunk("\n")));
                for (int i = 0; i < Math.min(topSellingItems.size(), 5); i++) {
                    TopSellingItemDTO item = topSellingItems.get(i);
                    document.add(new Paragraph(
                            (i + 1) + ". " + item.getItemName() + ": " + item.getTotalSold() + " units", normalFont
                    ));
                }
                document.add(new Paragraph(new Chunk("\n")));
            }

            // --- Strategic Suggestions (Text) ---
            document.add(new Paragraph("Strategic Suggestions for Improvement", headingFont));
            document.add(new Paragraph(new Chunk("\n")));

            if (!topSellingItems.isEmpty()) {
                Paragraph suggestion1 = new Paragraph(
                        "1. Focus on Top Performers: Consider increasing the production and stock of your top-selling items like " +
                                topSellingItems.get(0).getItemName() + " to meet high demand and capitalize on their popularity. Promoting these items in your marketing efforts could further boost sales.",
                        normalFont);
                document.add(suggestion1);
                document.add(new Paragraph(new Chunk("\n")));

                List<TopSellingItemDTO> lowSellingItems = topSellingItems.stream()
                        .sorted(Comparator.comparingLong(TopSellingItemDTO::getTotalSold))
                        .collect(Collectors.toList());

                if (lowSellingItems.size() > 1) {
                    TopSellingItemDTO itemForDiscount = lowSellingItems.get(0);
                    Paragraph suggestion2 = new Paragraph(
                            "2. Offer Discounts to Stimulate Sales: To encourage sales of lower-performing items, you could run a special promotion or discount on items like " +
                                    itemForDiscount.getItemName() + ". This can help to move inventory and attract new customers who are looking for a deal.",
                            normalFont);
                    document.add(suggestion2);
                    document.add(new Paragraph(new Chunk("\n")));
                }

                Paragraph suggestion3 = new Paragraph(
                        "3. Implement Cross-Selling: Strategically bundle top-selling items with lower-performing ones. For example, when a customer orders " +
                                topSellingItems.get(0).getItemName() + ", offer a small discount on a lesser-known item to increase its visibility and sales.",
                        normalFont);
                document.add(suggestion3);
                document.add(new Paragraph(new Chunk("\n")));

            } else {
                Paragraph suggestion = new Paragraph(
                        "There are no sales to analyze. To improve sales, you could consider running special promotions or introducing new products to attract customers.",
                        normalFont);
                document.add(suggestion);
                document.add(new Paragraph(new Chunk("\n")));
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

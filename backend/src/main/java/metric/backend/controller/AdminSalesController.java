package metric.backend.controller;

import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import metric.backend.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSalesData(
            @RequestParam(required = false) Integer dailyMonth,
            @RequestParam(required = false) Integer dailyYear,
            @RequestParam(required = false) Integer monthlyYear) {

        Map<String, Object> response = new HashMap<>();

        BigDecimal totalSales = orderRepository.findAll().stream()
                .filter(o -> o.getTotalAmount() != null && !"cancelled".equalsIgnoreCase(String.valueOf(o.getStatus())))
                .map(o -> o.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.put("totalMonthIncome", totalSales);
        response.put("totalWeekIncome", totalSales);
        response.put("totalIncome", totalSales);
        response.put("totalSales", totalSales);
        response.put("dailySales", Collections.emptyList());
        response.put("monthlySales", Collections.emptyList());
        response.put("history", Collections.emptyList());

        return ResponseEntity.ok(response);
    }
}
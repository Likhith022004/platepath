package com.fooddelivery.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.dto.response.*;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.Restaurant;
import com.fooddelivery.enums.OrderStatus;
import com.fooddelivery.repository.MenuItemRepository;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.RestaurantRepository;
import com.fooddelivery.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AIServiceImpl implements AIService {

    private static final String MODEL          = "claude-sonnet-4-20250514";
    private static final int    MAX_TOKENS     = 1024;
    private static final int    MAX_INPUT_CHARS = 3000;

    private final WebClient            aiWebClient;
    private final ObjectMapper         objectMapper;
    private final OrderRepository      orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository   menuItemRepository;
    private final UserRepository       userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public AIServiceImpl(@Qualifier("aiWebClient") WebClient aiWebClient,
                         ObjectMapper objectMapper,
                         OrderRepository orderRepository,
                         RestaurantRepository restaurantRepository,
                         MenuItemRepository menuItemRepository,
                         UserRepository userRepository,
                         SimpMessagingTemplate messagingTemplate) {
        this.aiWebClient         = aiWebClient;
        this.objectMapper        = objectMapper;
        this.orderRepository     = orderRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository  = menuItemRepository;
        this.userRepository      = userRepository;
        this.messagingTemplate   = messagingTemplate;
    }

    // ===== Feature 1: Food Recommendations =====

    @Override
    public List<RecommendationResponse> getRecommendations(Long customerId) {
        try {
            List<Order> recent = orderRepository
                    .findByCustomer_IdOrderByPlacedAtDesc(customerId)
                    .stream().limit(10).collect(Collectors.toList());

            String history = recent.stream()
                    .flatMap(o -> o.getItems().stream()
                            .map(i -> i.getMenuItem().getName() + " from " + o.getRestaurant().getName()))
                    .collect(Collectors.joining(", "));
            if (history.isBlank()) history = "No previous orders";

            String restaurantList = restaurantRepository.findAll().stream()
                    .map(r -> r.getName() + " (" + r.getCuisineType() + ")")
                    .collect(Collectors.joining(", "));

            String prompt = "You are a food recommendation AI for PlatePath app. " +
                    "Customer order history: " + truncate(history, MAX_INPUT_CHARS) + ". " +
                    "Available restaurants: " + truncate(restaurantList, 500) + ". " +
                    "Recommend 5 menu items. Return ONLY a JSON array, no markdown fences:\n" +
                    "[{\"itemName\":\"...\",\"restaurantName\":\"...\",\"reason\":\"...\",\"cuisineType\":\"...\",\"estimatedPrice\":0.0}]";

            return parseJsonList(callClaude(prompt), RecommendationResponse.class);

        } catch (Exception e) {
            log.warn("AI recommendations failed for customer {}: {}", customerId, e.getMessage());
            return getFallbackRecommendations();
        }
    }

    // ===== Feature 2: ETA Prediction =====

    @Override
    public ETAPredictionResponse predictETA(Long orderId) {
        try {
            Order order = orderRepository.findById(Objects.requireNonNull(orderId))
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
            int prepMins    = order.getRestaurant().getAvgPrepTimeMinutes();
            long queueSize  = orderRepository.findByStatus(OrderStatus.PREPARING).size();

            String prompt = "Predict food delivery ETA precisely. " +
                    "Restaurant prep time: " + prepMins + " minutes. " +
                    "Orders ahead in queue: " + queueSize + ". " +
                    "Average delivery distance: 5 km in Bengaluru traffic. " +
                    "Return ONLY JSON, no markdown fences:\n" +
                    "{\"estimatedMinutes\":30,\"confidence\":\"HIGH\"," +
                    "\"breakdown\":{\"prepTime\":20,\"waitTime\":5,\"deliveryTime\":15}}";

            return parseJson(callClaude(prompt), ETAPredictionResponse.class);

        } catch (Exception e) {
            log.warn("AI ETA prediction failed for order {}: {}", orderId, e.getMessage());
            int fallback = orderRepository.findById(Objects.requireNonNull(orderId))
                    .map(o -> o.getRestaurant().getAvgPrepTimeMinutes() + 30)
                    .orElse(45);
            return ETAPredictionResponse.builder()
                    .estimatedMinutes(fallback)
                    .confidence("LOW")
                    .breakdown(Map.of("prepTime", fallback - 20, "waitTime", 5, "deliveryTime", 15))
                    .build();
        }
    }

    // ===== Feature 3: Route Optimization =====

    @Override
    public RouteOptimizationResponse optimizeRoute(Long deliveryBoyId) {
        try {
            List<Order> active = orderRepository.findByDeliveryBoy_IdAndStatusIn(
                    deliveryBoyId,
                    List.of(OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY));

            if (active.isEmpty()) {
                return RouteOptimizationResponse.builder()
                        .optimizedSequence(List.of())
                        .totalEstimatedMinutes(0)
                        .totalDistanceKm(0.0)
                        .build();
            }

            String addresses = active.stream()
                    .map(o -> "Order#" + o.getId() + ": " + o.getDeliveryAddress())
                    .collect(Collectors.joining("; "));

            String prompt = "Optimize delivery route for a delivery boy in Bengaluru, India. " +
                    "Starting location: Koramangala, Bengaluru. " +
                    "Pending deliveries: " + truncate(addresses, MAX_INPUT_CHARS) + ". " +
                    "Return ONLY JSON, no markdown fences:\n" +
                    "{\"optimizedSequence\":[{\"order\":1,\"address\":\"...\",\"estimatedMinutes\":10}]," +
                    "\"totalEstimatedMinutes\":45,\"totalDistanceKm\":12.5}";

            return parseJson(callClaude(prompt), RouteOptimizationResponse.class);

        } catch (Exception e) {
            log.warn("AI route optimization failed for delivery boy {}: {}", deliveryBoyId, e.getMessage());
            List<Order> fallback = orderRepository.findByDeliveryBoy_IdAndStatusIn(
                    deliveryBoyId, List.of(OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY));
            List<RouteOptimizationResponse.RouteStop> stops = new ArrayList<>();
            for (int i = 0; i < fallback.size(); i++) {
                stops.add(new RouteOptimizationResponse.RouteStop(
                        i + 1, fallback.get(i).getDeliveryAddress(), 15 + i * 10));
            }
            return RouteOptimizationResponse.builder()
                    .optimizedSequence(stops)
                    .totalEstimatedMinutes(stops.size() * 15)
                    .totalDistanceKm((double) stops.size() * 3.0)
                    .build();
        }
    }

    // ===== Feature 4: Customer Preference Analysis =====

    @Override
    public CustomerPreferenceResponse analyzeCustomerPreferences(Long customerId) {
        try {
            List<Order> orders = orderRepository.findByCustomer_IdOrderByPlacedAtDesc(customerId);
            if (orders.isEmpty()) return getDefaultPreferences();

            String history = orders.stream()
                    .map(o -> o.getRestaurant().getName() + " [" + o.getRestaurant().getCuisineType() + "]: " +
                            o.getItems().stream()
                                    .map(i -> i.getMenuItem().getName() + " x" + i.getQuantity())
                                    .collect(Collectors.joining(", ")) +
                            " [₹" + o.getTotalAmount() + ", hour=" + o.getPlacedAt().getHour() + "]")
                    .collect(Collectors.joining("; "));

            String prompt = "Analyze food ordering behavior for a PlatePath customer. " +
                    "Order history: " + truncate(history, MAX_INPUT_CHARS) + ". " +
                    "Return ONLY JSON, no markdown fences:\n" +
                    "{\"topCuisines\":[\"Indian\",\"Chinese\"]," +
                    "\"frequentItems\":[\"Butter Chicken\"]," +
                    "\"preferredOrderTime\":\"Evening\"," +
                    "\"averageOrderValue\":350.0," +
                    "\"orderFrequency\":\"Weekly\"," +
                    "\"personalityType\":\"Comfort Food Lover\"}";

            return parseJson(callClaude(prompt), CustomerPreferenceResponse.class);

        } catch (Exception e) {
            log.warn("AI customer analysis failed for customer {}: {}", customerId, e.getMessage());
            return getDefaultPreferences();
        }
    }

    // ===== Feature 5: Delay Detection (scheduled every 60s) =====

    @Override
    @Scheduled(fixedDelay = 60_000)
    public void detectDelays() {
        log.debug("AI delay detection scan running...");
        try {
            List<Order> delayed = orderRepository.findDelayedOrders(
                    List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY),
                    LocalDateTime.now());

            for (Order order : delayed) {
                if (order.getEstimatedDeliveryTime() == null) continue;
                try {
                    long delayMins = ChronoUnit.MINUTES.between(
                            order.getEstimatedDeliveryTime(), LocalDateTime.now());
                    if (delayMins < 5) continue; // ignore tiny skews

                    String prompt = "Order #" + order.getId() +
                            " for customer " + order.getCustomer().getName() + " is potentially delayed. " +
                            "Expected delivery: " + order.getEstimatedDeliveryTime() +
                            ". Current time: " + LocalDateTime.now() +
                            ". Status: " + order.getStatus() +
                            ". Delay: " + delayMins + " minutes. " +
                            "Return ONLY JSON, no markdown fences:\n" +
                            "{\"isDelayed\":true,\"delayMinutes\":" + delayMins +
                            ",\"suggestedAction\":\"...\",\"customerMessage\":\"...\"}";

                    DelayDetectionResponse delay = parseJson(callClaude(prompt), DelayDetectionResponse.class);

                    if (Boolean.TRUE.equals(delay.getIsDelayed())) {
                        log.info("Delay detected for order #{}: {} min late", order.getId(), delay.getDelayMinutes());
                        messagingTemplate.convertAndSend(
                                "/topic/order/" + order.getId() + "/delay",
                                Objects.requireNonNull(delay));
                    }
                } catch (Exception inner) {
                    log.warn("Delay check failed for order #{}: {}", order.getId(), inner.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("AI delay scan failed: {}", e.getMessage());
        }
    }

    // ===== Feature 6: Analytics Dashboard =====

    @Override
    public AnalyticsResponse generateAnalytics(String period) {
        LocalDateTime since = switch (period.toUpperCase()) {
            case "DAILY"   -> LocalDateTime.now().minusDays(1);
            case "MONTHLY" -> LocalDateTime.now().minusMonths(1);
            default        -> LocalDateTime.now().minusWeeks(1);
        };

        // Always compute raw metrics regardless of AI availability
        long totalOrders    = orderRepository.countOrdersSince(since);
        BigDecimal revenue  = orderRepository.sumRevenueDeliveredSince(since);
        if (revenue == null) revenue = BigDecimal.ZERO;
        BigDecimal avg      = totalOrders > 0
                ? revenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Order> periodOrders = orderRepository.findAll().stream()
                .filter(o -> o.getPlacedAt() != null && o.getPlacedAt().isAfter(since))
                .collect(Collectors.toList());

        Map<String, Long> byStatus = periodOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getStatus().name(), Collectors.counting()));

        try {
            String aggregated = objectMapper.writeValueAsString(Map.of(
                    "period", period, "totalOrders", totalOrders,
                    "totalRevenue", revenue, "avgOrderValue", avg,
                    "ordersByStatus", byStatus));

            String prompt = "Generate actionable business insights for PlatePath food delivery. " +
                    "Period: " + period + ". Metrics: " + truncate(aggregated, MAX_INPUT_CHARS) + ". " +
                    "Return ONLY JSON, no markdown fences:\n" +
                    "{\"summary\":\"...\",\"keyInsights\":[\"...\",\"...\",\"...\"]," +
                    "\"topRestaurants\":[{\"name\":\"...\",\"orders\":0,\"revenue\":0.0}]," +
                    "\"peakHour\":\"7 PM\",\"recommendation\":\"...\",\"revenueGrowthTrend\":\"UP\"}";

            AnalyticsResponse result = parseJson(callClaude(prompt), AnalyticsResponse.class);
            result.setPeriod(period);
            result.setGeneratedAt(LocalDateTime.now());
            result.setTotalOrders(totalOrders);
            result.setTotalRevenue(revenue);
            result.setAvgOrderValue(avg);
            result.setOrdersByStatus(byStatus);
            return result;

        } catch (Exception e) {
            log.warn("AI analytics failed for period {}: {}", period, e.getMessage());
            return AnalyticsResponse.builder()
                    .period(period)
                    .generatedAt(LocalDateTime.now())
                    .totalOrders(totalOrders)
                    .totalRevenue(revenue)
                    .avgOrderValue(avg)
                    .ordersByStatus(byStatus)
                    .summary("AI insights unavailable. Raw metrics shown.")
                    .keyInsights(List.of("Total orders: " + totalOrders, "Revenue: ₹" + revenue))
                    .revenueGrowthTrend("UNKNOWN")
                    .build();
        }
    }

    // ===== Core Claude API Call =====

    private String callClaude(String prompt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", MODEL);
        body.put("max_tokens", MAX_TOKENS);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

        log.debug("Calling Claude API (prompt length={})", prompt.length());

        String raw = aiWebClient.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Empty response from AI API");
        }

        try {
            JsonNode root = objectMapper.readTree(raw);
            String text = root.path("content").get(0).path("text").asText();
            log.debug("Claude response (first 200 chars): {}", text.substring(0, Math.min(200, text.length())));
            return text;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response: " + raw.substring(0, Math.min(500, raw.length())));
        }
    }

    // ===== JSON Parsing Helpers =====

    private <T> T parseJson(String json, Class<T> clazz) throws JsonProcessingException {
        String clean = json.replaceAll("(?s)```json\\s*", "")
                          .replaceAll("(?s)```\\s*", "")
                          .trim();
        return objectMapper.readValue(clean, clazz);
    }

    private <T> List<T> parseJsonList(String json, Class<T> clazz) throws JsonProcessingException {
        String clean = json.replaceAll("(?s)```json\\s*", "")
                          .replaceAll("(?s)```\\s*", "")
                          .trim();
        JavaType type = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, clazz);
        return objectMapper.readValue(clean, type);
    }

    private String truncate(String input, int max) {
        if (input == null) return "";
        return input.length() > max ? input.substring(0, max) + "..." : input;
    }

    // ===== Fallback Helpers =====

    private List<RecommendationResponse> getFallbackRecommendations() {
        return menuItemRepository.findAll().stream()
                .limit(5)
                .map(m -> RecommendationResponse.builder()
                        .itemName(m.getName())
                        .restaurantName(m.getRestaurant().getName())
                        .reason("Popular item")
                        .cuisineType(m.getRestaurant().getCuisineType().name())
                        .estimatedPrice(m.getPrice().doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private CustomerPreferenceResponse getDefaultPreferences() {
        return CustomerPreferenceResponse.builder()
                .topCuisines(List.of())
                .frequentItems(List.of())
                .preferredOrderTime("Unknown")
                .averageOrderValue(0.0)
                .orderFrequency("Unknown")
                .personalityType("Explorer")
                .build();
    }
}

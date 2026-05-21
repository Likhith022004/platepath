package com.fooddelivery.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @Value("${platepath.maps.api-key:}")
    private String mapsApiKey;

    @GetMapping("/")
    public String index() {
        return "redirect:/restaurants";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/restaurants")
    public String browseRestaurants() {
        return "restaurant/browse";
    }

    @GetMapping("/restaurants/{id}/menu")
    public String restaurantMenu(@PathVariable Long id, Model model) {
        model.addAttribute("restaurantId", id);
        return "restaurant/menu";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "order/checkout";
    }

    @GetMapping("/orders/{id}/track")
    public String trackOrder(@PathVariable Long id, Model model) {
        model.addAttribute("orderId", id);
        model.addAttribute("mapsApiKey", mapsApiKey);
        return "order/track";
    }

    @GetMapping("/orders/history")
    public String orderHistory() {
        return "order/history";
    }

    @GetMapping("/delivery/panel")
    public String deliveryPanel(Model model) {
        model.addAttribute("mapsApiKey", mapsApiKey);
        return "delivery/panel";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/restaurant/manage")
    public String restaurantManage() {
        return "restaurant/manage";
    }
}

package com.fooddelivery.entity;

import com.fooddelivery.enums.CuisineType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurant_cuisine", columnList = "cuisine_type"),
        @Index(name = "idx_restaurant_owner", columnList = "owner_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine_type", nullable = false, length = 30)
    private CuisineType cuisineType;

    @Column(precision = 3)
    @Builder.Default
    private Double rating = 0.0;

    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private Boolean isOpen = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "delivery_radius_km")
    @Builder.Default
    private Double deliveryRadiusKm = 10.0;

    @Column(name = "avg_prep_time_minutes")
    @Builder.Default
    private Integer avgPrepTimeMinutes = 30;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

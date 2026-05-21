package com.fooddelivery.dto.response;

import com.fooddelivery.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {

    private String token;

    @Builder.Default
    private String tokenType = "Bearer";

    private long expiresIn;   // milliseconds until expiry

    private Long userId;
    private String userName;
    private String userEmail;
    private UserRole userRole;
}

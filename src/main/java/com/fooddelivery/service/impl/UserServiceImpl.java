package com.fooddelivery.service.impl;

import com.fooddelivery.dto.response.UserSummaryResponse;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.UserRole;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.repository.UserRepository;
import com.fooddelivery.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSummaryResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public UserSummaryResponse getUserById(Long id) {
        User user = userRepository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toSummary(user);
    }

    private UserSummaryResponse toSummary(User u) {
        return UserSummaryResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .address(u.getAddress())
                .role(u.getRole())
                .createdAt(u.getCreatedAt())
                .build();
    }
}

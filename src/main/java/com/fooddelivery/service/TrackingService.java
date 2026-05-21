package com.fooddelivery.service;

import com.fooddelivery.dto.request.UpdateStatusRequest;
import com.fooddelivery.dto.response.TrackingUpdateResponse;

import java.util.List;

public interface TrackingService {

    TrackingUpdateResponse updateOrderStatus(Long orderId, UpdateStatusRequest dto, String deliveryBoyEmail);

    List<TrackingUpdateResponse> getTrackingHistory(Long orderId, String requesterEmail);
}

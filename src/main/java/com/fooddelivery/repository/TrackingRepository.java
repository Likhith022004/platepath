package com.fooddelivery.repository;

import com.fooddelivery.entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingRepository extends JpaRepository<TrackingUpdate, Long> {

    List<TrackingUpdate> findByOrder_IdOrderByTimestampAsc(Long orderId);

    @Query("SELECT t FROM TrackingUpdate t WHERE t.order.id = :orderId " +
           "ORDER BY t.timestamp DESC LIMIT 1")
    Optional<TrackingUpdate> findLatestByOrderId(@Param("orderId") Long orderId);
}

package com.userservice.dao;

import com.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {
    long countByUserIdAndActiveTrue(Long userId);

    List<PaymentCard> findByUserId(Long userId);

    @Query(value = "SELECT * FROM payment_cards WHERE number = :number", nativeQuery = true)
    Optional<PaymentCard> findByNumber(@Param("number") String number);

    @Modifying
    @Query("UPDATE PaymentCard c SET c.active = :status WHERE c.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") boolean status);
}
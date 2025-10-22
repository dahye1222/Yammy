package com.example.yammy.payment.repository;
import com.example.yammy.payment.entity.UsedItem;
import org.springframework.data.jpa.repository.JpaRepository;

// DB 조회 기능 제공
public interface UsedItemRepository extends JpaRepository<UsedItem, Long> {
}

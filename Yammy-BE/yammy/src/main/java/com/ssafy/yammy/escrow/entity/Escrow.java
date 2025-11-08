package com.ssafy.yammy.escrow.entity;

import com.ssafy.yammy.auth.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "escrow")
public class Escrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;  // 송금하는 사람 (구매자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;    // 받는 사람 (판매자)

    @Column(nullable = false)
    private Long amount;        // 에스크로 금액

    @Column(nullable = false, length = 100)
    private String roomKey;     // 채팅방 키

    @Column(name = "used_item_id")
    private Long usedItemId;    // 중고 게시물 ID (선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EscrowStatus status;  // 에스크로 상태

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = EscrowStatus.PENDING;
        }
    }

    // 에스크로 완료 처리
    public void complete() {
        this.status = EscrowStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // 에스크로 취소 처리
    public void cancel() {
        this.status = EscrowStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
}

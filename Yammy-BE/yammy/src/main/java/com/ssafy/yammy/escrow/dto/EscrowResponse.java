package com.ssafy.yammy.escrow.dto;

import com.ssafy.yammy.escrow.entity.Escrow;
import com.ssafy.yammy.escrow.entity.EscrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscrowResponse {

    private Long escrowId;
    private Long fromMemberId;
    private String fromMemberNickname;
    private Long toMemberId;
    private String toMemberNickname;
    private Long amount;
    private String roomKey;
    private Long usedItemId;
    private EscrowStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static EscrowResponse from(Escrow escrow) {
        return EscrowResponse.builder()
                .escrowId(escrow.getId())
                .fromMemberId(escrow.getFromMember().getMemberId())
                .fromMemberNickname(escrow.getFromMember().getNickname())
                .toMemberId(escrow.getToMember().getMemberId())
                .toMemberNickname(escrow.getToMember().getNickname())
                .amount(escrow.getAmount())
                .roomKey(escrow.getRoomKey())
                .usedItemId(escrow.getUsedItemId())
                .status(escrow.getStatus())
                .createdAt(escrow.getCreatedAt())
                .completedAt(escrow.getCompletedAt())
                .build();
    }
}

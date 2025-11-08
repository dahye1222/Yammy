package com.ssafy.yammy.escrow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EscrowDepositRequest {

    private String roomKey;      // 채팅방 키
    private Long amount;         // 송금 금액
    private Long usedItemId;     // 중고 게시물 ID (선택)
    private Long toMemberId;     // 받는 사람 (판매자) ID
}

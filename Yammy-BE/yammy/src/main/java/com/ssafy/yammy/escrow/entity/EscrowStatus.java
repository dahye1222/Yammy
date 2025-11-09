package com.ssafy.yammy.escrow.entity;

public enum EscrowStatus {
    PENDING,      // 에스크로 대기 중 (구매자가 송금했으나 판매자가 아직 받지 않음)
    COMPLETED,    // 에스크로 완료 (판매자가 받기를 클릭하여 송금 완료)
    CANCELLED     // 에스크로 취소 (거래 취소 시)
}

package com.ssafy.yammy.escrow.repository;

import com.ssafy.yammy.escrow.entity.Escrow;
import com.ssafy.yammy.escrow.entity.EscrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    // 특정 채팅방의 모든 에스크로 조회
    List<Escrow> findByRoomKey(String roomKey);

    // 특정 상태의 에스크로 조회
    List<Escrow> findByStatus(EscrowStatus status);

    // 특정 회원이 송금한 에스크로 조회 (Member의 memberId 필드 사용)
    List<Escrow> findByFromMember_MemberId(Long fromMemberId);

    // 특정 회원이 받을 에스크로 조회 (Member의 memberId 필드 사용)
    List<Escrow> findByToMember_MemberId(Long toMemberId);
}

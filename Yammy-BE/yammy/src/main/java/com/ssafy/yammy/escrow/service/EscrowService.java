package com.ssafy.yammy.escrow.service;

import com.ssafy.yammy.auth.entity.Member;
import com.ssafy.yammy.auth.repository.MemberRepository;
import com.ssafy.yammy.escrow.dto.EscrowDepositRequest;
import com.ssafy.yammy.escrow.dto.EscrowResponse;
import com.ssafy.yammy.escrow.entity.Escrow;
import com.ssafy.yammy.escrow.entity.EscrowStatus;
import com.ssafy.yammy.escrow.repository.EscrowRepository;
import com.ssafy.yammy.payment.entity.Point;
import com.ssafy.yammy.payment.entity.PointTransaction;
import com.ssafy.yammy.payment.repository.PointRepository;
import com.ssafy.yammy.payment.repository.PointTransactionRepository;
import com.ssafy.yammy.useditemchat.service.UsedItemFirebaseChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EscrowService {

    private final EscrowRepository escrowRepository;
    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final UsedItemFirebaseChatService firebaseChatService;

    /**
     * 에스크로 송금 (구매자 → 에스크로)
     */
    @Transactional
    public EscrowResponse deposit(Long fromMemberId, EscrowDepositRequest request) {
        try {
            // 1. 회원 조회
            Member fromMember = memberRepository.findById(fromMemberId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "송금자를 찾을 수 없습니다."));

            Member toMember = memberRepository.findById(request.getToMemberId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "수신자를 찾을 수 없습니다."));

            // 2. 송금자 포인트 조회
            Point fromPoint = pointRepository.findByMember(fromMember)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "포인트 계좌가 없습니다."));

            // 3. 잔액 확인
            if (fromPoint.getBalance() < request.getAmount()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잔액이 부족합니다.");
            }

            // 4. 포인트 차감
            fromPoint.setBalance(fromPoint.getBalance() - request.getAmount());
            fromPoint.setUpdatedAt(LocalDateTime.now());
            pointRepository.save(fromPoint);

            // 5. Escrow 엔티티 생성
            Escrow escrow = Escrow.builder()
                    .fromMember(fromMember)
                    .toMember(toMember)
                    .amount(request.getAmount())
                    .roomKey(request.getRoomKey())
                    .usedItemId(request.getUsedItemId())
                    .status(EscrowStatus.PENDING)
                    .build();

            escrow = escrowRepository.save(escrow);

            // 6. PointTransaction 기록 (ESCROW_DEPOSIT)
            PointTransaction depositTransaction = PointTransaction.builder()
                    .tossPayment(null)  // 에스크로는 toss 결제가 아님
                    .point(fromPoint)
                    .member(fromMember)
                    .type("ESCROW_DEPOSIT")
                    .amount(-request.getAmount())  // 차감은 음수
                    .referenceId(escrow.getId().intValue())
                    .createdAt(LocalDateTime.now())
                    .build();
            pointTransactionRepository.save(depositTransaction);

            // 7. Firebase 에스크로 메시지 저장
            firebaseChatService.saveEscrowMessage(
                    request.getRoomKey(),
                    fromMemberId,
                    fromMember.getNickname(),
                    escrow.getId(),
                    request.getAmount()
            );

            log.info("✅ Escrow deposit completed: escrowId={}, fromMember={}, toMember={}, amount={}",
                    escrow.getId(), fromMemberId, request.getToMemberId(), request.getAmount());

            return EscrowResponse.from(escrow);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Escrow deposit failed: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "송금 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 에스크로 받기 (에스크로 → 판매자)
     */
    @Transactional
    public EscrowResponse release(Long escrowId, Long sellerId) {
        try {
            // 1. Escrow 조회
            Escrow escrow = escrowRepository.findById(escrowId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "에스크로를 찾을 수 없습니다."));

            // 2. 권한 확인 (판매자만 받을 수 있음)
            if (!escrow.getToMember().getMemberId().equals(sellerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수신 권한이 없습니다.");
            }

            // 3. 상태 확인 (PENDING만 받을 수 있음)
            if (escrow.getStatus() != EscrowStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 처리된 에스크로입니다.");
            }

            // 4. 판매자 포인트 조회
            Member toMember = escrow.getToMember();
            Point toPoint = pointRepository.findByMember(toMember)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "포인트 계좌가 없습니다."));

            // 5. 판매자 포인트 증가
            toPoint.setBalance(toPoint.getBalance() + escrow.getAmount());
            toPoint.setUpdatedAt(LocalDateTime.now());
            pointRepository.save(toPoint);

            // 6. Escrow 상태 업데이트 (COMPLETED)
            escrow.complete();
            escrowRepository.save(escrow);

            // 7. PointTransaction 기록 (ESCROW_RELEASE)
            PointTransaction releaseTransaction = PointTransaction.builder()
                    .tossPayment(null)  // 에스크로는 toss 결제가 아님
                    .point(toPoint)
                    .member(toMember)
                    .type("ESCROW_RELEASE")
                    .amount(escrow.getAmount())  // 증가는 양수
                    .referenceId(escrow.getId().intValue())
                    .createdAt(LocalDateTime.now())
                    .build();
            pointTransactionRepository.save(releaseTransaction);

            // 8. Firebase 메시지 상태 업데이트
            firebaseChatService.updateEscrowMessageStatus(
                    escrow.getRoomKey(),
                    escrow.getId(),
                    "completed"
            );

            log.info("✅ Escrow release completed: escrowId={}, toMember={}, amount={}",
                    escrow.getId(), sellerId, escrow.getAmount());

            return EscrowResponse.from(escrow);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Escrow release failed: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "수신 처리 중 오류가 발생했습니다.");
        }
    }
}

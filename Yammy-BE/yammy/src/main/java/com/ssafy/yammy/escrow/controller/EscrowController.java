package com.ssafy.yammy.escrow.controller;

import com.ssafy.yammy.config.JwtTokenProvider;
import com.ssafy.yammy.escrow.dto.EscrowDepositRequest;
import com.ssafy.yammy.escrow.dto.EscrowResponse;
import com.ssafy.yammy.escrow.service.EscrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Escrow API", description = "중고거래 에스크로 송금 API")
@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
public class EscrowController {

    private final EscrowService escrowService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "에스크로 송금", description = "구매자가 판매자에게 에스크로로 송금합니다.")
    @PostMapping("/deposit")
    public ResponseEntity<EscrowResponse> deposit(
            HttpServletRequest request,
            @RequestBody EscrowDepositRequest depositRequest
    ) {
        System.out.println("[EscrowController] deposit 호출됨");
        System.out.println("[EscrowController] Request: " + depositRequest);
        Long memberId = extractMemberIdFromToken(request);
        System.out.println("[EscrowController] memberId: " + memberId);
        EscrowResponse response = escrowService.deposit(memberId, depositRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "에스크로 받기", description = "판매자가 에스크로 금액을 받습니다.")
    @PostMapping("/release/{escrowId}")
    public ResponseEntity<EscrowResponse> release(
            HttpServletRequest request,
            @PathVariable Long escrowId
    ) {
        Long memberId = extractMemberIdFromToken(request);
        EscrowResponse response = escrowService.release(escrowId, memberId);
        return ResponseEntity.ok(response);
    }

    // JWT 토큰에서 memberId 추출
    private Long extractMemberIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 토큰이 없습니다.");
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.getMemberId(token);
    }
}

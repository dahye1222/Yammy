package com.ssafy.yammy.nft.controller;

import com.ssafy.yammy.config.CustomUserDetails;
import com.ssafy.yammy.nft.dto.NftMintRequest;
import com.ssafy.yammy.nft.dto.NftMintResponse;
import com.ssafy.yammy.nft.service.NftService;
import com.ssafy.yammy.ticket.entity.Ticket;
import com.ssafy.yammy.ticket.repository.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/nft")
@RequiredArgsConstructor
@Tag(name = "NFT", description = "NFT 발급 API")
public class NftController {

    private final NftService nftService;
    private final TicketRepository ticketRepository;

    /**
     * 기존 티켓에 대한 NFT 재발급
     */
    @PostMapping("/mint")
    @Operation(summary = "NFT 발급", description = "기존 티켓에 대해 NFT를 발급합니다.")
    public ResponseEntity<NftMintResponse> mintNft(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") NftMintRequest request,
            @RequestPart(value = "photo", required = false) org.springframework.web.multipart.MultipartFile photo) {

        Long memberId = userDetails.getMemberId();
        log.info("NFT 발급 요청 - memberId: {}, ticketId: {}", memberId, request.getTicketId());

        // 티켓 조회 및 권한 확인
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new RuntimeException("티켓을 찾을 수 없습니다."));

        if (!ticket.getMember().getMemberId().equals(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        if (Boolean.TRUE.equals(ticket.getNftMinted())) {
            throw new RuntimeException("이미 NFT가 발급된 티켓입니다.");
        }

        // NFT 발급
        NftMintResponse response = nftService.mintTicketNft(ticket, request.getWalletAddress(), photo);

        if (response.isSuccess()) {
            ticket.markNftMinted(
                    response.getTokenId(),
                    response.getMetadataUri(),
                    response.getTransactionHash()
            );

            // IPFS 이미지 해시 저장
            if (response.getImageIpfsHash() != null) {
                ticket.setIpfsImageHash(response.getImageIpfsHash());
            }

            ticketRepository.save(ticket);
        }

        return ResponseEntity.ok(response);
    }
}

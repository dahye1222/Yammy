package com.ssafy.yammy.ticket.service;

import com.ssafy.yammy.auth.entity.Member;
import com.ssafy.yammy.auth.repository.MemberRepository;
import com.ssafy.yammy.nft.dto.NftMintResponse;
import com.ssafy.yammy.nft.service.NftService;
import com.ssafy.yammy.ticket.dto.TicketRequest;
import com.ssafy.yammy.ticket.dto.TicketResponse;
import com.ssafy.yammy.ticket.entity.Ticket;
import com.ssafy.yammy.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final MemberRepository memberRepository;
    private final NftService nftService;

    /**
     * 티켓 생성
     */
    @Transactional
    public TicketResponse createTicket(Long memberId, TicketRequest request, String photoUrl) {
        return createTicket(memberId, request, photoUrl, false);
    }

    /**
     * 티켓 생성 (NFT 발급 옵션 포함)
     */
    @Transactional
    public TicketResponse createTicket(Long memberId, TicketRequest request, String photoUrl, boolean mintNft) {
        return createTicketWithPhoto(memberId, request, null, mintNft);
    }

    /**
     * 티켓 생성 (NFT 발급 옵션 및 사진 포함)
     * 모든 사진은 Pinata IPFS에 저장됨
     */
    @Transactional
    public TicketResponse createTicketWithPhoto(Long memberId, TicketRequest request,
                                                 org.springframework.web.multipart.MultipartFile photo, boolean mintNft) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Ticket ticket = Ticket.builder()
                .member(member)
                .matchcode(request.getMatchcode())
                .game(request.getGame())
                .date(request.getDate())
                .location(request.getLocation())
                .seat(request.getSeat())
                .comment(request.getComment())
                .type(request.getType())
                .awayScore(request.getAwayScore())
                .homeScore(request.getHomeScore())
                .review(request.getReview())
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("티켓 생성 완료 - ticketId: {}, memberId: {}", savedTicket.getTicketId(), memberId);

        // 1. 사진을 Pinata에 업로드하고 IPFS 해시 저장 (NFT 발급 여부 관계없이 항상 업로드)
        if (photo != null && !photo.isEmpty()) {
            try {
                String imageHash = nftService.uploadImageToPinata(photo, "yammy-ticket-" + savedTicket.getTicketId());
                savedTicket.setIpfsImageHash(imageHash);
                log.info("이미지 IPFS 업로드 완료 - ticketId: {}, imageHash: {}", savedTicket.getTicketId(), imageHash);
            } catch (Exception e) {
                log.error("이미지 IPFS 업로드 실패 - ticketId: {}", savedTicket.getTicketId(), e);
                // 이미지 업로드 실패해도 티켓은 생성됨
            }
        }

        // 2. NFT 발급 (선택사항) - 이미 업로드된 이미지 해시 재사용
        if (mintNft && member.getWalletAddress() != null) {
            try {
                NftMintResponse response = nftService.mintTicketNftWithHash(
                        savedTicket,
                        member.getWalletAddress(),
                        savedTicket.getIpfsImageHash()
                );

                if (response.isSuccess()) {
                    savedTicket.markNftMinted(
                            response.getTokenId(),
                            response.getMetadataUri(),
                            response.getTransactionHash()
                    );

                    log.info("NFT 발급 완료 - ticketId: {}, tokenId: {}",
                            savedTicket.getTicketId(), response.getTokenId());
                } else {
                    log.warn("NFT 발급 실패 (티켓은 생성됨) - ticketId: {}, error: {}",
                            savedTicket.getTicketId(), response.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("NFT 발급 실패 (티켓은 생성됨) - ticketId: {}",
                        savedTicket.getTicketId(), e);
                // NFT 발급 실패해도 티켓은 생성됨
            }
        } else if (mintNft && member.getWalletAddress() == null) {
            log.warn("NFT 발급 요청되었으나 지갑 주소가 없음 - memberId: {}", memberId);
        }

        return TicketResponse.from(savedTicket);
    }

    /**
     * 사용자의 티켓 목록 조회
     */
    public List<TicketResponse> getMyTickets(Long memberId) {
        List<Ticket> tickets = ticketRepository.findByMember_MemberIdOrderByCreatedAtDesc(memberId);
        return tickets.stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 티켓 상세 조회
     */
    public TicketResponse getTicket(Long ticketId, Long memberId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("티켓을 찾을 수 없습니다."));

        // 본인 티켓만 조회 가능
        if (!ticket.getMember().getMemberId().equals(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        return TicketResponse.from(ticket);
    }

    /**
     * 티켓 수정
     */
    @Transactional
    public TicketResponse updateTicket(Long ticketId, Long memberId, TicketRequest request, String photoUrl) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("티켓을 찾을 수 없습니다."));

        // 본인 티켓만 수정 가능
        if (!ticket.getMember().getMemberId().equals(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        ticket.update(
                request.getGame(),
                request.getDate(),
                request.getLocation(),
                request.getSeat(),
                request.getComment(),
                request.getType(),
                request.getAwayScore(),
                request.getHomeScore(),
                request.getReview(),
                photoUrl,
                request.getMatchcode()
        );

        log.info("티켓 수정 완료 - ticketId: {}, memberId: {}", ticketId, memberId);

        return TicketResponse.from(ticket);
    }

    /**
     * 티켓 삭제
     */
    @Transactional
    public void deleteTicket(Long ticketId, Long memberId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("티켓을 찾을 수 없습니다."));

        // 본인 티켓만 삭제 가능
        if (!ticket.getMember().getMemberId().equals(memberId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        ticketRepository.delete(ticket);
        log.info("티켓 삭제 완료 - ticketId: {}, memberId: {}", ticketId, memberId);
    }
}

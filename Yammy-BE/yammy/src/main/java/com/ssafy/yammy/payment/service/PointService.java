package com.ssafy.yammy.payment.service;

import com.ssafy.yammy.auth.entity.Member;
import com.ssafy.yammy.auth.repository.MemberRepository;
import com.ssafy.yammy.payment.dto.PointResponse;
import com.ssafy.yammy.payment.entity.Point;
import com.ssafy.yammy.payment.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    // 내 포인트 조회
    public PointResponse getMyPoint(Long memberId) {
        // 멤버 id 찾기
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."));

        // 해당 회원의 얌 포인트 조회
        Point myPoint = pointRepository.findByMember(member)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "포인트 계좌가 없습니다."));


        // 잔액 조회
        PointResponse response = new PointResponse();
        response.setBalance(myPoint.getBalance());
        return response;
    }

    // 포인트 사용
    public PointResponse use(Long memberId, Long amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 회원입니다."));

        Point myPoint = pointRepository.findByMember(member)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "포인트 계좌가 없습니다."));

        // 잔액 조회
        PointResponse response = new PointResponse();
        response.setBalance(myPoint.getBalance());
        return response;
    }
}

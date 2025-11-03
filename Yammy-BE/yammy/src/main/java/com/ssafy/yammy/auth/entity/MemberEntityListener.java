package com.ssafy.yammy.auth.entity;

import com.ssafy.yammy.payment.entity.Point;
import com.ssafy.yammy.payment.repository.PointRepository;
import jakarta.persistence.PostPersist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Member 엔티티의 생명주기 이벤트를 처리하는 리스너
 * Member가 생성되면 자동으로 Point 계좌를 생성합니다.
 */
@Component
public class MemberEntityListener {

    private static PointRepository pointRepository;

    /**
     * Spring Bean으로 등록된 PointRepository를 정적 필드에 주입
     * EntityListener는 JPA에 의해 관리되므로 static 필드를 사용해야 합니다.
     */
    @Autowired
    public void init(PointRepository pointRepository) {
        MemberEntityListener.pointRepository = pointRepository;
    }

    /**
     * Member가 DB에 저장된 직후 자동으로 실행됩니다.
     * Member와 1:1 관계인 Point 계좌를 생성합니다.
     *
     * @param member 저장된 Member 엔티티
     */
    @PostPersist
    public void createPointAccount(Member member) {
        Point point = new Point();
        point.setMember(member);
        point.setBalance(0L);
        point.setUpdatedAt(LocalDateTime.now());
        pointRepository.save(point);
    }
}

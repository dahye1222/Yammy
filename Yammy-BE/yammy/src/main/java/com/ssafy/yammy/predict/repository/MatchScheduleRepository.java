package com.ssafy.yammy.predict.repository;

import com.ssafy.yammy.predict.entity.MatchSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {

    // 특정 날짜의 경기 목록 조회
    List<MatchSchedule> findByMatchDate(String matchDate);
}

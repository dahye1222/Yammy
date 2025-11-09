package com.ssafy.yammy.predict.controller;

import com.ssafy.yammy.predict.dto.MatchScheduleResponse;
import com.ssafy.yammy.predict.service.PredictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/predict")
@RequiredArgsConstructor
@Tag(name = "Predict", description = "승부예측 API")
public class PredictController {

    private final PredictService predictService;

    /**
     * 특정 날짜의 경기 목록 조회
     */
    @GetMapping("/matches")
    @Operation(summary = "날짜별 경기 조회", description = "특정 날짜의 예정된 경기 목록을 조회합니다.")
    public ResponseEntity<List<MatchScheduleResponse>> getMatchesByDate(
            @RequestParam 
            @Parameter(description = "경기 날짜 (YYYYMMDD 형식)", example = "20251110") 
            String date) {
        
        log.info("날짜별 경기 조회 요청 - date: {}", date);
        
        List<MatchScheduleResponse> matches = predictService.getMatchesByDate(date);
        
        log.info("조회된 경기 수: {}", matches.size());
        
        return ResponseEntity.ok(matches);
    }
}

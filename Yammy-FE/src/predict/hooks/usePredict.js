import { useState, useEffect } from 'react';
import { getTodayMatches, getMatchesByDate } from '../api/predictApi';

// 승부예측 관련 커스텀 훅
export const usePredict = () => {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // 백엔드 데이터를 프론트엔드 형식으로 변환
  const transformMatchData = (backendMatch) => {
    return {
      id: backendMatch.id,
      homeTeam: backendMatch.home,
      awayTeam: backendMatch.away,
      gameTime: '18:30', // 기본값
      stadium: '-', // 백엔드에 stadium 정보가 없으므로 기본값
      date: backendMatch.matchDate,
      matchStatus: backendMatch.matchStatus,
      gameid: backendMatch.gameid,
      year: backendMatch.year,
      homeWinningRate: 1.00, // 기본값
      awayWinningRate: 1.00, // 기본값
    };
  };

  // 오늘의 경기 데이터 가져오기 (백엔드 API 사용)
  const fetchTodayMatches = async () => {
    try {
      setLoading(true);
      
      const backendMatches = await getTodayMatches();
      
      // 백엔드 데이터를 프론트엔드 형식으로 변환
      const transformedMatches = backendMatches.map(transformMatchData);
      
      setMatches(transformedMatches);
      setError(null);
      
      console.log('🎯 변환된 경기 데이터:', transformedMatches);
    } catch (err) {
      setError('경기 데이터를 불러오는데 실패했습니다.');
      console.error('Error fetching matches:', err);
      
      // 에러 발생 시 빈 배열로 설정
      setMatches([]);
    } finally {
      setLoading(false);
    }
  };

  // 특정 날짜의 경기 데이터 가져오기
  const fetchMatchesByDate = async (date) => {
    try {
      setLoading(true);
      
      const backendMatches = await getMatchesByDate(date);
      
      // 백엔드 데이터를 프론트엔드 형식으로 변환
      const transformedMatches = backendMatches.map(transformMatchData);
      
      setMatches(transformedMatches);
      setError(null);
      
      console.log(`🎯 ${date} 경기 데이터:`, transformedMatches);
    } catch (err) {
      setError('경기 데이터를 불러오는데 실패했습니다.');
      console.error('Error fetching matches:', err);
      
      // 에러 발생 시 빈 배열로 설정
      setMatches([]);
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트 시 오늘 데이터 로드
  useEffect(() => {
    fetchTodayMatches();
  }, []);

  return {
    matches,
    loading,
    error,
    fetchTodayMatches,
    fetchMatchesByDate
  };
};

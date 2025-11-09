import axios from "axios";

const BASE_URL = "http://localhost:8080/api";

// axios 인스턴스 생성 (predict는 인증이 필요 없으므로 기본 axios 사용)
const predictApi = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

/**
 * 특정 날짜의 경기 목록 조회
 * @param {string} date - 경기 날짜 (YYYYMMDD 형식, 예: "20251110")
 * @returns {Promise} 경기 목록 데이터
 */
export const getMatchesByDate = async (date) => {
  try {
    console.log(`🎯 승부예측 경기 조회 요청: ${date}`);
    
    const response = await predictApi.get(`/predict/matches`, {
      params: { date }
    });
    
    console.log(`✅ 승부예측 경기 조회 성공:`, response.data);
    return response.data;
  } catch (error) {
    console.error(`❌ 승부예측 경기 조회 실패 (${date}):`, error);
    throw error;
  }
};

/**
 * 오늘 날짜의 경기 목록 조회
 * @returns {Promise} 오늘 경기 목록 데이터
 */
export const getTodayMatches = async () => {
  try {
    // 오늘 날짜를 YYYYMMDD 형식으로 변환
    const today = new Date();
    const formattedDate = today.getFullYear() + 
                         String(today.getMonth() + 1).padStart(2, '0') + 
                         String(today.getDate()).padStart(2, '0');
    
    console.log(`📅 오늘 날짜: ${formattedDate}`);
    
    return await getMatchesByDate(formattedDate);
  } catch (error) {
    console.error(`❌ 오늘 경기 조회 실패:`, error);
    throw error;
  }
};

/**
 * 특정 날짜 문자열을 YYYYMMDD 형식으로 변환
 * @param {string} dateString - 날짜 문자열 (예: "2025-11-10")
 * @returns {string} YYYYMMDD 형식 날짜
 */
export const formatDateForAPI = (dateString) => {
  return dateString.replace(/-/g, '');
};

export default {
  getMatchesByDate,
  getTodayMatches,
  formatDateForAPI
};

// 팀별 컬러 매핑 (bgcolor1, textcolor1)
export const TEAM_COLORS = {
  '키움': { bgColor: '#570514', textColor: '#ffffff' },
  '두산': { bgColor: '#1A1748', textColor: '#ffffff' },
  '롯데': { bgColor: '#041E42', textColor: '#ffffff' },
  '삼성': { bgColor: '#074CA1', textColor: '#ffffff' },
  '한화': { bgColor: '#FC4E00', textColor: '#ffffff' },
  'KIA': { bgColor: '#EA0029', textColor: '#ffffff' },
  'LG': { bgColor: '#C30452', textColor: '#ffffff' },
  'SSG': { bgColor: '#CF0E20', textColor: '#ffffff' },
  'NC': { bgColor: '#315288', textColor: '#ffffff' },
  'KT': { bgColor: '#000000', textColor: '#ffffff' }
};

// localStorage에서 팀 정보 가져오기
export const getUserTeam = () => {
  return localStorage.getItem('team') || localStorage.getItem('favTeam') || null;
};

// 팀 컬러 가져오기 (기본값: 초록색)
export const getTeamColors = () => {
  const team = getUserTeam();
  return TEAM_COLORS[team] || { bgColor: '#4CAF50', textColor: '#ffffff' };
};

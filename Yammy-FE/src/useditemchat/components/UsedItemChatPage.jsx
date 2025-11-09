import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { usedItemChatApi } from '../api/usedItemChatApi';
import { getUsedItemById } from '../../useditem/api/usedItemApi';
import { useUsedItemChatMessages } from '../hooks/useUsedItemChatMessages';
import { getMyPoint } from '../../payment/api/pointAPI';
import { deposit } from '../api/escrowApi';
import useAuthStore from '../../stores/authStore';
import UsedItemMessageList from './UsedItemMessageList';
import UsedItemChatInput from './UsedItemChatInput';
import TransferModal from './TransferModal';
import '../styles/UsedItemChatPage.css';

/**
 * 중고거래 1:1 채팅 페이지
 */
export default function UsedItemChatPage() {
  const { roomKey } = useParams(); // 채팅방 키
  const navigate = useNavigate();
  const user = useAuthStore((state) => state.user); // 현재 로그인한 사용자

  const [chatRoomInfo, setChatRoomInfo] = useState(null); // 채팅방 정보
  const [itemInfo, setItemInfo] = useState(null); // 물품 정보
  const [loading, setLoading] = useState(true); // 로딩 상태
  const [error, setError] = useState(null); // 에러 메시지
  const [selectedImage, setSelectedImage] = useState(null); // 클릭한 이미지 확대
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false); // 송금 모달
  const [myBalance, setMyBalance] = useState(0); // 내 포인트 잔액

  // 실시간 메시지 구독
  const { messages, loading: loadingMessages, error: messageError } = useUsedItemChatMessages(roomKey);

  // 채팅방 정보 및 물품 정보 로드
  useEffect(() => {
    if (!roomKey) return;

    const initChat = async () => {
      try {
        setLoading(true);

        // 1. 채팅방 정보 조회
        const chatRoom = await usedItemChatApi.getChatRoom(roomKey);
        console.log('Chat room:', chatRoom);
        setChatRoomInfo(chatRoom);

        // 2. 물품 정보 조회
        const item = await getUsedItemById(chatRoom.usedItemId);
        console.log('Item info:', item);
        setItemInfo(item);

        // 3. 내 포인트 잔액 조회
        const pointData = await getMyPoint();
        setMyBalance(pointData.balance);

        setLoading(false);
      } catch (err) {
        console.error('Error initializing chat:', err);
        setError(err.response?.data?.message || err.message || '채팅방을 불러올 수 없습니다.');
        setLoading(false);
      }
    };

    initChat();
  }, [roomKey]);

  // 송금 모달 열기
  const handleOpenTransferModal = () => {
    setIsTransferModalOpen(true);
  };

  // 송금 모달 닫기
  const handleCloseTransferModal = () => {
    setIsTransferModalOpen(false);
  };

  // 송금 처리
  const handleTransferSubmit = async (amount) => {
    try {
      if (!user || !user.memberId) {
        alert('로그인이 필요합니다.');
        return;
      }

      // 상대방 memberId 계산 (채팅방 참여자 중 나를 제외한 사람)
      // localStorage에서 가져온 값은 string이므로 Number로 변환
      const myMemberId = Number(user.memberId);
      const otherMemberId = chatRoomInfo.buyerId === myMemberId
        ? chatRoomInfo.sellerId
        : chatRoomInfo.buyerId;

      console.log('송금 정보:', { roomKey, amount, myMemberId, otherMemberId, usedItemId: chatRoomInfo.usedItemId });

      await deposit(roomKey, amount, otherMemberId, chatRoomInfo.usedItemId);

      alert('송금이 완료되었습니다.');

      // 포인트 잔액 갱신
      const updatedPoint = await getMyPoint();
      setMyBalance(updatedPoint.balance);

      // pointUpdated 이벤트 발생 (NavigationBarTop 업데이트용)
      window.dispatchEvent(new Event('pointUpdated'));
    } catch (error) {
      console.error('송금 실패:', error);
      alert(error.response?.data?.message || '송금에 실패했습니다.');
    }
  };

  // 에러 처리
  if (error || messageError) {
    return (
      <div className="chat-error-container">
        <div className="chat-error-box">
          <div className="chat-error-icon">⚠️</div>
          <h2 className="chat-error-title">채팅방 오류</h2>
          <p className="chat-error-message">{error || messageError}</p>
          <button onClick={() => navigate('/chatlist')} className="chat-error-button">
            채팅방 목록으로 돌아가기
          </button>
        </div>
      </div>
    );
  }

  // 로딩 중
  if (loading) {
    return (
      <div className="chat-loading-container">
        <div className="chat-loading-box">
          <div className="chat-spinner"></div>
          <p className="chat-loading-text">채팅방 입장 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="chat-page">
      {/* 헤더: 물품 정보 */}
      <div className="chat-header">
        <div className="chat-header-inner">
          <div className="chat-header-content">
            {/* 뒤로가기 버튼 */}
            <button onClick={() => navigate('/chatlist')} className="chat-back-button">
              <svg className="chat-back-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>

            {/* 물품 정보 */}
            {itemInfo && (
              <div className="chat-item-info">
                {itemInfo.imageUrls && itemInfo.imageUrls[0] && (
                  <img src={itemInfo.imageUrls[0]} alt={itemInfo.title} className="chat-item-image" />
                )}
                <div className="chat-item-text">
                  <h2 className="chat-item-title">{itemInfo.title}</h2>
                  <p className="chat-item-price">{itemInfo.price?.toLocaleString()}원</p>
                </div>
              </div>
            )}

            {/* 송금 버튼 */}
            {chatRoomInfo && (
              <button className="chat-transfer-btn" onClick={handleOpenTransferModal}>
                송금
              </button>
            )}
          </div>
        </div>
      </div>

      {/* 메시지 목록 */}
      <div className="chat-message-area">
        <UsedItemMessageList
          messages={messages}
          loading={loadingMessages}
          onImageClick={(url) => setSelectedImage(url)}
        />
      </div>

      {/* 입력창 */}
      {roomKey && <UsedItemChatInput roomKey={roomKey} />}

      {/* 이미지 확대 모달 */}
      {selectedImage && (
        <div className="chat-image-modal" onClick={() => setSelectedImage(null)}>
          <div className="chat-image-modal-inner">
            <button onClick={() => setSelectedImage(null)} className="chat-image-close">
              <svg className="chat-close-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
            <img
              src={selectedImage}
              alt="확대 보기"
              className="chat-image-full"
              onClick={(e) => e.stopPropagation()}
            />
          </div>
        </div>
      )}

      {/* 송금 모달 */}
      <TransferModal
        isOpen={isTransferModalOpen}
        onClose={handleCloseTransferModal}
        onSubmit={handleTransferSubmit}
        currentBalance={myBalance}
      />
    </div>
  );
}

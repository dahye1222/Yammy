import api from "../../api/apiClient"

/**
 * 에스크로 송금
 * @param {string} roomKey - 채팅방 키
 * @param {number} amount - 송금 금액
 * @param {number} toMemberId - 받는 사람 ID
 * @param {number} usedItemId - 중고 게시물 ID (선택)
 */
export const deposit = async (roomKey, amount, toMemberId, usedItemId = null) => {
  const response = await api.post("/escrow/deposit", {
    roomKey,
    amount,
    toMemberId,
    usedItemId,
  })
  return response.data
}

/**
 * 에스크로 받기
 * @param {number} escrowId - 에스크로 ID
 */
export const release = async (escrowId) => {
  const response = await api.post(`/escrow/release/${escrowId}`)
  return response.data
}

import { useState } from "react"
import { release } from "../api/escrowApi"
import "../styles/EscrowMessage.css"

function EscrowMessageItem({ message, isMine }) {
  const [isProcessing, setIsProcessing] = useState(false)
  const format = (num) => num.toLocaleString()

  const handleRelease = async () => {
    if (isProcessing) return
    if (!window.confirm("송금을 받으시겠습니까?")) return

    setIsProcessing(true)
    try {
      await release(message.escrowId)
      alert("송금이 완료되었습니다.")
      // Firebase onSnapshot이 자동으로 UI를 업데이트할 것임
    } catch (error) {
      console.error("송금 받기 실패:", error)
      alert(error.response?.data?.message || "송금 받기에 실패했습니다.")
    } finally {
      setIsProcessing(false)
    }
  }

  const isCompleted = message.status === "completed"
  const isPending = message.status === "pending"

  return (
    <div className={`escrow-message-item ${isMine ? "mine" : "other"}`}>
      <div className="escrow-message-card">
        <div className="escrow-message-icon">💰</div>
        <div className="escrow-message-content">
          <div className="escrow-message-header">
            <span className="escrow-message-label">
              {isMine ? "송금했습니다" : "송금을 받았습니다"}
            </span>
            <span
              className={`escrow-message-status ${
                isCompleted ? "completed" : "pending"
              }`}
            >
              {isCompleted ? "완료" : "대기중"}
            </span>
          </div>
          <div className="escrow-message-amount">{format(message.amount)}얌</div>
          {!isMine && isPending && (
            <button
              className="escrow-message-receive-btn"
              onClick={handleRelease}
              disabled={isProcessing}
            >
              {isProcessing ? "처리중..." : "받기"}
            </button>
          )}
          {isCompleted && message.completedAt && (
            <div className="escrow-message-time">
              {new Date(
                message.completedAt.seconds * 1000
              ).toLocaleString()}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default EscrowMessageItem

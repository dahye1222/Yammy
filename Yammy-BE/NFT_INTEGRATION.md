# Yammy 티켓 NFT 통합 가이드

## 📋 개요

Yammy 티켓을 블록체인 NFT로 발급하여 영구적인 소유권을 보장하는 기능입니다.

## 🚀 구현된 기능

### 1. 백엔드 (Spring Boot)

#### 엔티티 변경사항
- **Ticket 엔티티**: NFT 관련 필드 추가
  - `nftTokenId`: NFT 토큰 ID
  - `nftMinted`: NFT 발급 여부
  - `nftMetadataUri`: IPFS 메타데이터 URI
  - `nftTransactionHash`: 발급 트랜잭션 해시
  - `nftMintedAt`: NFT 발급 시간

- **Member 엔티티**: 지갑 주소 필드 추가
  - `walletAddress`: Ethereum 지갑 주소 (0x...)

#### 새로운 패키지 구조
```
com.ssafy.yammy.nft/
├── config/
│   ├── NftConfig.java          # NFT 설정
│   └── IpfsConfig.java         # IPFS(Pinata) 설정
├── dto/
│   └── NftMintResult.java      # NFT 발급 결과 DTO
└── service/
    └── NftService.java         # NFT 발급 서비스
```

#### API 엔드포인트

**티켓 생성 (NFT 발급 옵션 포함)**
```http
POST /api/tickets?mintNft=true
Content-Type: multipart/form-data

{
  "ticket": {
    "game": "기아 vs 삼성",
    "date": "2025-11-07",
    "location": "광주-기아 챔피언스 필드",
    "seat": "3루 207블록 5열 11번",
    "comment": "최고의 경기였어요!",
    ...
  },
  "photo": <file>
}
```

**응답**
```json
{
  "id": 123,
  "game": "기아 vs 삼성",
  "date": "2025-11-07",
  ...
  "nftMinted": true,
  "nftTokenId": 0,
  "nftMetadataUri": "ipfs://QmXXX...",
  "nftTransactionHash": "0xabc123...",
  "nftMintedAt": "2025-11-07T10:30:00"
}
```

## 🔧 환경 설정

### 1. 환경 변수 설정 (.env)

```bash
# NFT 관련 설정
NFT_CONTRACT_ADDRESS=0xC3411A874DC352569927A57556E3b35682CfaEBE
NFT_NETWORK=sepolia
NFT_RPC_URL=https://rpc.sepolia.org
NFT_PRIVATE_KEY=your_private_key_here
NFT_OWNER_ADDRESS=0xD8aC392887BEad44D2da842B4Dbb5b12d5c48CE6

# IPFS (Pinata) 설정
PINATA_API_KEY=your_pinata_api_key
PINATA_API_SECRET=your_pinata_api_secret
PINATA_GATEWAY=https://gateway.pinata.cloud/ipfs/
```

### 2. Pinata 계정 설정

1. https://pinata.cloud/ 가입
2. API Keys 생성
   - Dashboard > API Keys > New Key
   - Admin 권한 체크
   - Key Name: `yammy-nft`
3. API Key와 Secret을 `.env`에 추가

### 3. 개인키 설정

⚠️ **보안 주의사항**
- 테스트넷 전용 지갑 사용 권장
- 절대 실제 자산이 있는 지갑의 개인키 사용 금지
- `.env` 파일은 `.gitignore`에 반드시 추가

## 📊 데이터베이스 마이그레이션

### Ticket 테이블 변경
```sql
ALTER TABLE ticket
ADD COLUMN nft_token_id BIGINT,
ADD COLUMN nft_minted BOOLEAN DEFAULT FALSE,
ADD COLUMN nft_metadata_uri TEXT,
ADD COLUMN nft_transaction_hash TEXT,
ADD COLUMN nft_minted_at DATETIME;
```

### Member 테이블 변경
```sql
ALTER TABLE member
ADD COLUMN wallet_address VARCHAR(42);
```

## 🔄 NFT 발급 플로우

```
1. 사용자가 티켓 생성 (mintNft=true)
   ↓
2. Ticket DB에 저장 (ticketId 생성)
   ↓
3. 티켓 메타데이터 생성
   - 경기 정보, 사진, 속성 등
   ↓
4. IPFS(Pinata)에 메타데이터 업로드
   - JSON 형식으로 업로드
   - IPFS 해시 반환 (QmXXX...)
   ↓
5. 스마트 컨트랙트 mintTicket 호출
   - 파라미터: (userWallet, ticketId, ipfsUri)
   - Web3j 사용
   ↓
6. 블록체인 트랜잭션 전송
   - Sepolia 테스트넷
   - 가스비 자동 계산
   ↓
7. NFT 발급 완료
   - 트랜잭션 해시 반환
   - 이벤트에서 tokenId 추출
   ↓
8. DB 업데이트
   - nftTokenId, nftMinted, txHash 저장
```

## 📝 NFT 메타데이터 구조

```json
{
  "name": "Yammy Ticket #123",
  "description": "기아 vs 삼성 @ 광주-기아 챔피언스 필드 (2025-11-07)",
  "image": "https://yammy-bucket.s3.amazonaws.com/ticket/photo.jpg",
  "attributes": [
    {
      "trait_type": "Game",
      "value": "기아 vs 삼성"
    },
    {
      "trait_type": "Date",
      "value": "2025-11-07"
    },
    {
      "trait_type": "Location",
      "value": "광주-기아 챔피언스 필드"
    },
    {
      "trait_type": "Seat",
      "value": "3루 207블록 5열 11번"
    },
    {
      "trait_type": "Type",
      "value": "야구"
    },
    {
      "trait_type": "Score",
      "value": "5:3"
    }
  ],
  "properties": {
    "ticket_id": 123,
    "comment": "최고의 경기였어요!",
    "created_at": "2025-11-07T10:30:00Z"
  }
}
```

## 🧪 테스트

### 1. 의존성 설치
```bash
cd Yammy-BE/yammy
./gradlew clean build
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. API 테스트 (Postman/cURL)

**티켓 생성 (NFT 발급 O)**
```bash
curl -X POST http://localhost:8080/api/tickets?mintNft=true \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "ticket={\"game\":\"기아 vs 삼성\",\"date\":\"2025-11-07\",\"location\":\"광주\",\"seat\":\"207블록\",\"comment\":\"최고!\"}" \
  -F "photo=@ticket-photo.jpg"
```

**티켓 생성 (NFT 발급 X)**
```bash
curl -X POST http://localhost:8080/api/tickets \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "ticket={...}" \
  -F "photo=@ticket-photo.jpg"
```

## 🔗 배포된 스마트 컨트랙트 정보

### Sepolia 테스트넷
- **Contract Address**: `0xC3411A874DC352569927A57556E3b35682CfaEBE`
- **Network**: Sepolia (Ethereum Testnet)
- **Chain ID**: 11155111
- **Owner Address**: `0xD8aC392887BEad44D2da842B4Dbb5b12d5c48CE6`
- **Etherscan**: https://sepolia.etherscan.io/address/0xC3411A874DC352569927A57556E3b35682CfaEBE

### 스마트 컨트랙트 함수
```solidity
// NFT 발급
function mintTicket(
    address to,
    uint256 ticketId,
    string memory tokenURI
) public onlyOwner returns (uint256)

// 티켓 ID로 토큰 ID 조회
function getTokenIdByTicketId(uint256 ticketId)
    external view returns (uint256)

// 사용자 소유 NFT 조회
function getTokensByOwner(address owner)
    external view returns (uint256[] memory)
```

## 🎨 프론트엔드 통합

### 1. 지갑 주소 등록
사용자가 MetaMask 연결 후 지갑 주소를 백엔드에 저장해야 합니다.

```javascript
// 예시 코드
const connectWallet = async () => {
  const accounts = await window.ethereum.request({
    method: 'eth_requestAccounts'
  });

  const walletAddress = accounts[0];

  // 백엔드에 지갑 주소 업데이트
  await updateMemberWallet(walletAddress);
};
```

### 2. 티켓 생성 시 NFT 발급 옵션
```javascript
const createTicket = async (ticketData, mintNft = false) => {
  const formData = new FormData();
  formData.append('ticket', JSON.stringify(ticketData));
  formData.append('photo', photoFile);

  const response = await fetch(
    `/api/tickets?mintNft=${mintNft}`,
    {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${token}` },
      body: formData
    }
  );

  return await response.json();
};
```

### 3. NFT 정보 표시
```javascript
const TicketCard = ({ ticket }) => {
  return (
    <div>
      <h3>{ticket.game}</h3>
      {ticket.nftMinted && (
        <div className="nft-badge">
          <span>NFT 발급됨 #{ticket.nftTokenId}</span>
          <a href={`https://sepolia.etherscan.io/tx/${ticket.nftTransactionHash}`}>
            트랜잭션 보기
          </a>
        </div>
      )}
    </div>
  );
};
```

## 📚 참고 링크

- **Yammy-NFT 레포**: `C:\Users\SSAFY\Desktop\Yammy-NFT`
- **스마트 컨트랙트 가이드**: `Yammy-NFT/INTEGRATION_GUIDE.md`
- **Sepolia Etherscan**: https://sepolia.etherscan.io/
- **OpenSea Testnet**: https://testnets.opensea.io/
- **Pinata Docs**: https://docs.pinata.cloud/
- **Web3j Docs**: https://docs.web3j.io/

## ❗ 주의사항

### 보안
1. **개인키 관리**
   - 절대 Git에 커밋하지 말 것
   - 테스트넷 전용 지갑 사용
   - 환경 변수로만 관리

2. **컨트랙트 권한**
   - Owner만 NFT 발급 가능
   - 백엔드 서버에서만 발급

### 가스비
- Sepolia 테스트넷은 무료 (테스트 ETH 사용)
- Faucet에서 테스트 ETH 받기: https://sepoliafaucet.com

### NFT 발급 실패 처리
- NFT 발급 실패해도 티켓은 정상 생성됨
- 나중에 재발급 기능 구현 가능 (TODO)

## 🔜 다음 단계

- [ ] 사용자 지갑 연결 UI 구현 (프론트엔드)
- [ ] NFT 재발급 API 구현
- [ ] NFT 메타데이터 미리보기 기능
- [ ] OpenSea 메타데이터 최적화
- [ ] 메인넷 배포 (Polygon Mainnet)
- [ ] 배치 발급 기능 활용

## 🐛 문제 해결

### "insufficient funds" 에러
- 테스트넷 토큰이 부족합니다
- https://sepoliafaucet.com 에서 받으세요

### "nonce too high" 에러
```bash
# 캐시 삭제 후 재시작
rm -rf Yammy-NFT/cache Yammy-NFT/artifacts
```

### Pinata 업로드 실패
- API Key/Secret 확인
- Rate limit 확인 (무료 플랜 제한)

### Web3j 연결 실패
- RPC URL 확인
- 네트워크 상태 확인
- Sepolia 네트워크 다운 시 대체 RPC 사용
  - https://ethereum-sepolia.publicnode.com
  - https://1rpc.io/sepolia

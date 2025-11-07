package com.ssafy.yammy.nft.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ssafy.yammy.nft.dto.NftMintResponse;
import com.ssafy.yammy.ticket.entity.Ticket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NftService {

    @Value("${nft.contract-address}")
    private String contractAddress;

    @Value("${nft.rpc-url}")
    private String rpcUrl;

    @Value("${nft.private-key}")
    private String privateKey;

    @Value("${ipfs.api-key}")
    private String pinataApiKey;

    @Value("${ipfs.api-secret}")
    private String pinataApiSecret;

    @Value("${ipfs.gateway}")
    private String pinataGateway;

    private final Gson gson = new Gson();
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * 티켓 NFT 발급 (이미지 포함)
     */
    public NftMintResponse mintTicketNft(Ticket ticket, String userWalletAddress, MultipartFile photo) {
        try {
            log.info("NFT 발급 시작 - ticketId: {}, wallet: {}", ticket.getTicketId(), userWalletAddress);

            String imageIpfsUri = null;
            String imageHash = null;

            // 1. 이미지를 Pinata에 업로드
            if (photo != null && !photo.isEmpty()) {
                imageHash = uploadImageToPinata(photo, "yammy-ticket-" + ticket.getTicketId());
                imageIpfsUri = pinataGateway + imageHash;
                log.info("이미지 IPFS 업로드 완료: ipfs://{}", imageHash);
            }

            // 2. 메타데이터 생성 (IPFS 이미지 URL 포함)
            String metadata = createMetadata(ticket, imageIpfsUri);

            // 3. IPFS에 메타데이터 업로드 (Pinata)
            String metadataHash = uploadJsonToPinata(metadata, "yammy-ticket-" + ticket.getTicketId() + ".json");
            String metadataUri = "ipfs://" + metadataHash;
            log.info("메타데이터 IPFS 업로드 완료: {}", metadataUri);

            // 4. 스마트 컨트랙트 호출 (mintTicket)
            TransactionReceipt receipt = mintNftOnChain(
                userWalletAddress,
                ticket.getTicketId(),
                metadataUri
            );

            // 5. 토큰 ID 파싱 (이벤트에서 추출)
            Long tokenId = parseTokenIdFromReceipt(receipt);

            log.info("NFT 발급 완료 - ticketId: {}, tokenId: {}, txHash: {}",
                    ticket.getTicketId(), tokenId, receipt.getTransactionHash());

            return NftMintResponse.builder()
                    .tokenId(tokenId)
                    .metadataUri(metadataUri)
                    .transactionHash(receipt.getTransactionHash())
                    .imageIpfsHash(imageHash)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("NFT 발급 실패 - ticketId: {}", ticket.getTicketId(), e);
            return NftMintResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 티켓 NFT 발급 (이미 업로드된 이미지 해시 사용)
     */
    public NftMintResponse mintTicketNftWithHash(Ticket ticket, String userWalletAddress, String imageHash) {
        try {
            log.info("NFT 발급 시작 (기존 이미지 재사용) - ticketId: {}, wallet: {}, imageHash: {}",
                    ticket.getTicketId(), userWalletAddress, imageHash);

            String imageIpfsUri = null;
            if (imageHash != null) {
                imageIpfsUri = pinataGateway + imageHash;
            }

            // 1. 메타데이터 생성 (IPFS 이미지 URL 포함)
            String metadata = createMetadata(ticket, imageIpfsUri);

            // 2. IPFS에 메타데이터 업로드 (Pinata)
            String metadataHash = uploadJsonToPinata(metadata, "yammy-ticket-" + ticket.getTicketId() + ".json");
            String metadataUri = "ipfs://" + metadataHash;
            log.info("메타데이터 IPFS 업로드 완료: {}", metadataUri);

            // 3. 스마트 컨트랙트 호출 (mintTicket)
            TransactionReceipt receipt = mintNftOnChain(
                userWalletAddress,
                ticket.getTicketId(),
                metadataUri
            );

            // 4. 토큰 ID 파싱 (이벤트에서 추출)
            Long tokenId = parseTokenIdFromReceipt(receipt);

            log.info("NFT 발급 완료 - ticketId: {}, tokenId: {}, txHash: {}",
                    ticket.getTicketId(), tokenId, receipt.getTransactionHash());

            return NftMintResponse.builder()
                    .tokenId(tokenId)
                    .metadataUri(metadataUri)
                    .transactionHash(receipt.getTransactionHash())
                    .imageIpfsHash(imageHash)
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("NFT 발급 실패 - ticketId: {}", ticket.getTicketId(), e);
            return NftMintResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 메타데이터 JSON 생성
     */
    private String createMetadata(Ticket ticket, String imageIpfsUri) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "Yammy Ticket #" + ticket.getTicketId());
        metadata.put("description", String.format("%s @ %s (%s)",
                ticket.getGame(), ticket.getLocation(), ticket.getDate()));

        // 이미지 (IPFS URL 사용)
        if (imageIpfsUri != null) {
            metadata.put("image", imageIpfsUri);
        }

        // Attributes (OpenSea 형식)
        List<Map<String, String>> attributes = new ArrayList<>();
        attributes.add(Map.of("trait_type", "Game", "value", ticket.getGame()));
        attributes.add(Map.of("trait_type", "Date", "value", ticket.getDate().toString()));
        attributes.add(Map.of("trait_type", "Location", "value", ticket.getLocation()));
        attributes.add(Map.of("trait_type", "Seat", "value", ticket.getSeat()));

        if (ticket.getType() != null) {
            attributes.add(Map.of("trait_type", "Type", "value", ticket.getType()));
        }

        if (ticket.getHomeScore() != null && ticket.getAwayScore() != null) {
            attributes.add(Map.of("trait_type", "Score",
                    "value", ticket.getHomeScore() + ":" + ticket.getAwayScore()));
        }

        metadata.put("attributes", attributes);

        // Properties (추가 정보)
        Map<String, Object> properties = new HashMap<>();
        properties.put("ticket_id", ticket.getTicketId());
        properties.put("comment", ticket.getComment());
        properties.put("created_at", ticket.getCreatedAt().toString());
        metadata.put("properties", properties);

        return gson.toJson(metadata);
    }

    /**
     * Pinata IPFS에 이미지 파일 업로드
     */
    public String uploadImageToPinata(MultipartFile file, String fileName) throws IOException {
        RequestBody fileBody = RequestBody.create(
                file.getBytes(),
                MediaType.parse(file.getContentType())
        );

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName + ".jpg", fileBody)
                .addFormDataPart("pinataMetadata", gson.toJson(Map.of("name", fileName)))
                .build();

        Request request = new Request.Builder()
                .url("https://api.pinata.cloud/pinning/pinFileToIPFS")
                .post(requestBody)
                .addHeader("pinata_api_key", pinataApiKey)
                .addHeader("pinata_secret_api_key", pinataApiSecret)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Pinata 이미지 업로드 실패: " + response);
            }

            String responseBody = response.body().string();
            JsonObject result = gson.fromJson(responseBody, JsonObject.class);
            return result.get("IpfsHash").getAsString();
        }
    }

    /**
     * Pinata IPFS에 JSON 메타데이터 업로드
     */
    private String uploadJsonToPinata(String jsonContent, String fileName) throws IOException {
        Map<String, Object> pinataBody = new HashMap<>();
        pinataBody.put("pinataContent", gson.fromJson(jsonContent, Object.class));

        Map<String, String> pinataMetadata = new HashMap<>();
        pinataMetadata.put("name", fileName);
        pinataBody.put("pinataMetadata", pinataMetadata);

        RequestBody body = RequestBody.create(
                gson.toJson(pinataBody),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://api.pinata.cloud/pinning/pinJSONToIPFS")
                .post(body)
                .addHeader("pinata_api_key", pinataApiKey)
                .addHeader("pinata_secret_api_key", pinataApiSecret)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Pinata JSON 업로드 실패: " + response);
            }

            String responseBody = response.body().string();
            if (responseBody == null) {
                throw new IOException("Pinata 응답이 비어있습니다");
            }

            JsonObject result = gson.fromJson(responseBody, JsonObject.class);
            return result.get("IpfsHash").getAsString();
        }
    }

    /**
     * 블록체인에 NFT 발급
     */
    private TransactionReceipt mintNftOnChain(String toAddress, Long ticketId, String tokenUri)
            throws Exception {

        Web3j web3j = Web3j.build(new HttpService(rpcUrl));
        Credentials credentials = Credentials.create(privateKey);

        // mintTicket(address to, uint256 ticketId, string memory tokenURI)
        Function function = new Function(
                "mintTicket",
                Arrays.asList(
                        new Address(toAddress),
                        new Uint256(BigInteger.valueOf(ticketId)),
                        new Utf8String(tokenUri)
                ),
                Collections.emptyList()
        );

        String encodedFunction = FunctionEncoder.encode(function);

        // Sepolia Chain ID
        long chainId = 11155111L;

        RawTransactionManager txManager = new RawTransactionManager(
                web3j, credentials, chainId
        );

        EthSendTransaction ethSendTransaction = txManager.sendTransaction(
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                contractAddress,
                encodedFunction,
                BigInteger.ZERO
        );

        String transactionHash = ethSendTransaction.getTransactionHash();

        // 트랜잭션 영수증 대기
        Optional<TransactionReceipt> receiptOptional = web3j.ethGetTransactionReceipt(transactionHash)
                .send()
                .getTransactionReceipt();

        if (receiptOptional.isEmpty()) {
            // 영수증이 아직 없으면 잠시 대기 후 재시도
            Thread.sleep(5000);
            receiptOptional = web3j.ethGetTransactionReceipt(transactionHash)
                    .send()
                    .getTransactionReceipt();
        }

        return receiptOptional.orElseThrow(() ->
            new RuntimeException("트랜잭션 영수증을 받을 수 없습니다: " + transactionHash));
    }

    /**
     * 트랜잭션 영수증에서 토큰 ID 파싱
     * TicketMinted 이벤트: event TicketMinted(uint256 indexed tokenId, uint256 indexed ticketId, address indexed owner, string tokenURI)
     */
    private Long parseTokenIdFromReceipt(TransactionReceipt receipt) {
        // 이벤트 로그에서 tokenId 추출
        // 로그의 첫 번째 topic은 이벤트 시그니처, 두 번째가 tokenId (indexed)
        if (receipt.getLogs() != null && !receipt.getLogs().isEmpty()) {
            try {
                String tokenIdHex = receipt.getLogs().get(0).getTopics().get(1);
                return new BigInteger(tokenIdHex.substring(2), 16).longValue();
            } catch (Exception e) {
                log.warn("토큰 ID 파싱 실패, 0 반환", e);
                return 0L;
            }
        }
        return 0L;
    }
}

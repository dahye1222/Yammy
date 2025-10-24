package com.ssafy.yammy.payment.service;

import com.ssafy.yammy.payment.config.PhotoConfig;
import com.ssafy.yammy.payment.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Presigner; // S3의 presigned URL 요청 및 발급 도구
import software.amazon.awssdk.services.s3.model.PutObjectRequest; // “S3에 파일 올릴 때 어떤 버킷/경로/헤더로 올릴지
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID; // 랜덤 값 생성

@Service
@RequiredArgsConstructor // final 필드 사용 시
public class PhotoService {

    private final PhotoConfig photoConfig;

    // presigned URL 생성
    public PhotoUploadResponse getGalleryPresignedUploadUrl(Long memberId, String originalFilename, String contentType) {
        validateFile(originalFilename, contentType);

        try (S3Presigner presigner = createPresigner()) {

            // member 관련 코드 구현 시 수정 예정
            String folderName = (memberId != null) ? "members/" + memberId : "anonymous";
            String subFolder = "gallery";

            // 확장자 추출
            String extension = extractExtension(originalFilename);

            // .jpg, .png 같은 확장자 추출하기
            String key = String.format("%s/used-items/%s/%s/%s%s",
                    photoConfig.getActiveProfile(),
                    folderName,
                    subFolder,
                    UUID.randomUUID(), // 동일한 파일명 올렸을 경우 경로 다르게 지정하기 위함
                    extension);

            // 요청 객체
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(photoConfig.getBucketName())
                    .key(key)
                    .contentType(contentType)
                    .build();

            // presign 요청 생성
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(20)) // presignedUrl 경로에 업로드 가능 시간
                    .putObjectRequest(objectRequest)
                    .build();

            URL presignedUrl = presigner.presignPutObject(presignRequest).url();
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    photoConfig.getBucketName(),
                    photoConfig.getRegion(),
                    key);

            return new PhotoUploadResponse(presignedUrl.toString(), fileUrl);
        }
    }

    //
    private void validateFile(String originalFilename, String contentType) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 비어 있습니다.");
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }

     // 확장자 추출 (.jpg 등)
    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex > 0) ? filename.substring(dotIndex) : "";
    }


    // S3 Presigner 생성
    private S3Presigner createPresigner() {
        return S3Presigner.builder()
                .region(Region.of(photoConfig.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        photoConfig.getAccessKey(),
                                        photoConfig.getSecretKey()
                                )
                        )
                )
                .build();
    }
}

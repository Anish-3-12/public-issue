package com.publicissuetracker.service.impl;

import com.publicissuetracker.model.Issue;
import com.publicissuetracker.service.AttachmentService;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AttachmentServiceImpl — stores files in MinIO under:
 *   <bucketName>/<issueId>/<random>-originalName
 *
 * This implementation is tolerant at startup: if MinIO is unreachable when the
 * application starts, we log a warning and let the app start in degraded mode.
 * Uploads / listing will still fail later if MinIO is not reachable.
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    private final MinioClient minioClient;

    // Make sure this matches the bucket you created in MinIO. Change if needed.
    private final String bucketName = "issues";

    public AttachmentServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
        // Do NOT call ensureBucketExists() here — defer to @PostConstruct init to avoid constructor failures.
    }

    /**
     * Best-effort initialization invoked after construction. We try to ensure the bucket exists,
     * but if MinIO is unreachable we log a warning and allow the app to continue running.
     */
    @PostConstruct
    public void initMinio() {
        try {
            ensureBucketExists();
            log.info("MinIO bucket '{}' is ready.", bucketName);
        } catch (Exception e) {
            log.warn("Could not initialize MinIO bucket '{}' at startup — continuing without MinIO. Error: {}", bucketName, e.getMessage());
            // Debug-level stacktrace if needed:
            log.debug("Full exception initializing MinIO bucket", e);
        }
    }

    /**
     * Try to create the bucket if it does not exist. Throws exception on real failures.
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Created MinIO bucket '{}'.", bucketName);
            }
        } catch (Exception e) {
            // propagate to caller so callers can decide; initMinio() will catch and log instead of failing startup.
            throw new RuntimeException("Error checking/creating MinIO bucket: " + bucketName, e);
        }
    }

    @Override
    public void uploadAttachments(Issue issue, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            try (InputStream is = file.getInputStream()) {
                // build object name under issue id prefix
                String objectName = issue.getId() + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );

                log.debug("Uploaded object '{}' to bucket '{}'", objectName, bucketName);
            } catch (Exception e) {
                // Log contextual info then rethrow as runtime to surface to controller/service layer
                log.error("Error uploading file '{}' for issue {} to MinIO: {}", file.getOriginalFilename(), issue.getId(), e.getMessage());
                throw new RuntimeException("Error uploading file to MinIO", e);
            }
        }
    }

    @Override
    public List<String> listAttachments(Issue issue) {
        List<String> urls = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(issue.getId().toString() + "/")
                            .recursive(true)
                            .build()
            );

            for (Result<Item> result : results) {
                Item item = result.get();
                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .bucket(bucketName)
                                .object(item.objectName())
                                .method(Method.GET)
                                .build()
                );
                urls.add(url);
            }
        } catch (Exception e) {
            log.error("Error listing files for issue {} from MinIO: {}", issue.getId(), e.getMessage());
            throw new RuntimeException("Error listing files from MinIO", e);
        }
        return urls;
    }
}



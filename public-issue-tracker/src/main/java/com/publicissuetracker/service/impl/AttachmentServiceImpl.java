package com.publicissuetracker.service.impl;

import com.publicissuetracker.model.Issue;
import com.publicissuetracker.service.AttachmentService;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * AttachmentServiceImpl — stores files in MinIO under:
 *   <bucketName>/<issueId>/<random>-originalName
 *
 * listAttachments() lists objects by prefix issueId/ and returns presigned GET URLs.
 *
 * Adjust `bucketName` to match your MinIO bucket, or create the bucket in the MinIO UI.
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {

    private final MinioClient minioClient;
    // Make sure this matches the bucket you created in MinIO. Change if needed.
    private final String bucketName = "issues";

    public AttachmentServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
        } catch (Exception e) {
            // Log or ignore — we will fail later if bucket truly doesn't exist or credentials are wrong.
            // Throwing runtime here will make it obvious on start.
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
            } catch (Exception e) {
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
            throw new RuntimeException("Error listing files from MinIO", e);
        }
        return urls;
    }
}



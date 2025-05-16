package com.eidiko.movie_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        log.info("before uploading to cloudinary");
        Map<? ,?> upload = cloudinary.uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("folder", "movie_poster"));
        log.info("after uploading image - {}" ,upload.get("secure_url"));
        return upload.get("secure_url").toString();
    }
}

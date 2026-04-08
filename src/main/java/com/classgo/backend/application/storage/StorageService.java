package com.classgo.backend.application.storage;

import com.classgo.backend.api.uploads.dto.UploadDtos.UploadResponse;
import com.classgo.backend.domain.enums.FileCategory;
import com.classgo.backend.domain.model.MediaAsset;
import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.MediaAssetRepository;
import com.classgo.backend.domain.repository.UserRepository;
import com.classgo.backend.infrastructure.security.SecurityUtils;
import com.classgo.backend.infrastructure.storage.StorageGateway;
import com.classgo.backend.shared.exception.BusinessRuleViolationException;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/webp");
    private static final long MAX_SIZE = 2 * 1024 * 1024;

    private final StorageGateway storageGateway;
    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;

    public StorageService(
        StorageGateway storageGateway,
        MediaAssetRepository mediaAssetRepository,
        UserRepository userRepository
    ) {
        this.storageGateway = storageGateway;
        this.mediaAssetRepository = mediaAssetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UploadResponse uploadAvatar(MultipartFile file) {
        if (file.isEmpty() || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessRuleViolationException("Unsupported avatar format");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessRuleViolationException("Avatar exceeds 2MB");
        }
        User user = userRepository.findById(SecurityUtils.currentUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String key = "avatars/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        String publicUrl = storageGateway.upload(key, file);
        MediaAsset asset = new MediaAsset();
        asset.setOwnerUser(user);
        asset.setCategory(FileCategory.AVATAR);
        asset.setFileName(file.getOriginalFilename());
        asset.setContentType(file.getContentType());
        asset.setFileSize(file.getSize());
        asset.setStorageKey(key);
        asset.setPublicUrl(publicUrl);
        asset = mediaAssetRepository.save(asset);
        return new UploadResponse(asset.getId(), asset.getPublicUrl());
    }
}

package com.classgo.backend.api.uploads;

import com.classgo.backend.api.uploads.dto.UploadDtos.UploadResponse;
import com.classgo.backend.application.storage.StorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/uploads")
public class UploadController {

    private final StorageService storageService;

    public UploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/avatar")
    public UploadResponse uploadAvatar(@RequestPart("file") MultipartFile file) {
        return storageService.uploadAvatar(file);
    }
}

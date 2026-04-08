package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.FileCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "media_assets")
public class MediaAsset extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "owner_user_id")
    private User ownerUser;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileCategory category;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "content_type", nullable = false)
    private String contentType;
    @Column(name = "file_size", nullable = false)
    private long fileSize;
    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;
    @Column(name = "public_url", nullable = false)
    private String publicUrl;

    public User getOwnerUser() {
        return this.ownerUser;
    }

    public FileCategory getCategory() {
        return this.category;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public String getStorageKey() {
        return this.storageKey;
    }

    public String getPublicUrl() {
        return this.publicUrl;
    }

    public void setOwnerUser(final User ownerUser) {
        this.ownerUser = ownerUser;
    }

    public void setCategory(final FileCategory category) {
        this.category = category;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public void setFileSize(final long fileSize) {
        this.fileSize = fileSize;
    }

    public void setStorageKey(final String storageKey) {
        this.storageKey = storageKey;
    }

    public void setPublicUrl(final String publicUrl) {
        this.publicUrl = publicUrl;
    }
}

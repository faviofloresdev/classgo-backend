package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.MediaAsset;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
}

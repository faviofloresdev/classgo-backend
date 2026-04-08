package com.classgo.backend.domain.repository;

import com.classgo.backend.domain.model.AvatarCatalog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvatarCatalogRepository extends JpaRepository<AvatarCatalog, String> {
    List<AvatarCatalog> findAllByOrderBySortOrderAsc();
}

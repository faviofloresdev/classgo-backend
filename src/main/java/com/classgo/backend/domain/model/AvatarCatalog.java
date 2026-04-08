package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "avatars_catalog")
public class AvatarCatalog {
    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false)
    private String url;
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getCategory() {
        return this.category;
    }

    public String getUrl() {
        return this.url;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setSortOrder(final int sortOrder) {
        this.sortOrder = sortOrder;
    }
}

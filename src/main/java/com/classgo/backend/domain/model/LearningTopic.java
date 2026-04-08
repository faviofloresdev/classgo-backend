package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.TopicDifficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_topics")
public class LearningTopic extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column(length = 1000)
    private String description;
    private String icon;
    private String color;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicDifficulty difficulty;
    @Column(name = "questions_json", nullable = false, columnDefinition = "TEXT")
    private String questionsJson;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getIcon() {
        return this.icon;
    }

    public String getColor() {
        return this.color;
    }

    public TopicDifficulty getDifficulty() {
        return this.difficulty;
    }

    public String getQuestionsJson() {
        return this.questionsJson;
    }

    public User getTeacher() {
        return this.teacher;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    public void setColor(final String color) {
        this.color = color;
    }

    public void setDifficulty(final TopicDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setQuestionsJson(final String questionsJson) {
        this.questionsJson = questionsJson;
    }

    public void setTeacher(final User teacher) {
        this.teacher = teacher;
    }
}

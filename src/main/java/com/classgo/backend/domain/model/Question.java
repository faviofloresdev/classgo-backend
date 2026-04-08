package com.classgo.backend.domain.model;

import com.classgo.backend.domain.enums.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "questions")
public class Question extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;
    @Column(nullable = false, columnDefinition = "text")
    private String prompt;
    @Column(columnDefinition = "text")
    private String explanation;
    @Column(name = "difficulty_level")
    private Integer difficultyLevel;
    @Column(name = "sort_order")
    private Integer sortOrder;

    public Topic getTopic() {
        return this.topic;
    }

    public QuestionType getType() {
        return this.type;
    }

    public String getPrompt() {
        return this.prompt;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public Integer getDifficultyLevel() {
        return this.difficultyLevel;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public void setTopic(final Topic topic) {
        this.topic = topic;
    }

    public void setType(final QuestionType type) {
        this.type = type;
    }

    public void setPrompt(final String prompt) {
        this.prompt = prompt;
    }

    public void setExplanation(final String explanation) {
        this.explanation = explanation;
    }

    public void setDifficultyLevel(final Integer difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public void setSortOrder(final Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}

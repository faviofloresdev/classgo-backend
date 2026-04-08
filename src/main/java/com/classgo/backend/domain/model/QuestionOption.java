package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_options")
public class QuestionOption extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    @Column(name = "option_text", nullable = false)
    private String optionText;
    @Column(name = "is_correct", nullable = false)
    private boolean correct;
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public Question getQuestion() {
        return this.question;
    }

    public String getOptionText() {
        return this.optionText;
    }

    public boolean isCorrect() {
        return this.correct;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public void setQuestion(final Question question) {
        this.question = question;
    }

    public void setOptionText(final String optionText) {
        this.optionText = optionText;
    }

    public void setCorrect(final boolean correct) {
        this.correct = correct;
    }

    public void setSortOrder(final Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}

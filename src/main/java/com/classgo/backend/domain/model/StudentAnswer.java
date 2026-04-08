package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "student_answers", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "challenge_id", "question_id"}))
public class StudentAnswer extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @ManyToOne(optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private WeeklyChallenge challenge;
    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;
    @Column(name = "answer_text")
    private String answerText;
    @Column(name = "is_correct", nullable = false)
    private boolean correct;
    @Column(name = "points_earned", nullable = false)
    private int pointsEarned;
    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    public Student getStudent() {
        return this.student;
    }

    public WeeklyChallenge getChallenge() {
        return this.challenge;
    }

    public Question getQuestion() {
        return this.question;
    }

    public QuestionOption getSelectedOption() {
        return this.selectedOption;
    }

    public String getAnswerText() {
        return this.answerText;
    }

    public boolean isCorrect() {
        return this.correct;
    }

    public int getPointsEarned() {
        return this.pointsEarned;
    }

    public Instant getAnsweredAt() {
        return this.answeredAt;
    }

    public void setStudent(final Student student) {
        this.student = student;
    }

    public void setChallenge(final WeeklyChallenge challenge) {
        this.challenge = challenge;
    }

    public void setQuestion(final Question question) {
        this.question = question;
    }

    public void setSelectedOption(final QuestionOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public void setAnswerText(final String answerText) {
        this.answerText = answerText;
    }

    public void setCorrect(final boolean correct) {
        this.correct = correct;
    }

    public void setPointsEarned(final int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    public void setAnsweredAt(final Instant answeredAt) {
        this.answeredAt = answeredAt;
    }
}

package com.classgo.backend.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "student_attempts")
public class StudentAttempt extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private AppClassroom classroom;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private LearningTopic topic;
    @Column(name = "week_number", nullable = false)
    private int weekNumber;
    @Column(nullable = false)
    private int score;
    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;
    @Column(name = "time_spent", nullable = false)
    private int timeSpent;
    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;
    @Column(name = "answers_json", nullable = false, columnDefinition = "TEXT")
    private String answersJson;

    public User getStudent() {
        return this.student;
    }

    public AppClassroom getClassroom() {
        return this.classroom;
    }

    public LearningTopic getTopic() {
        return this.topic;
    }

    public int getWeekNumber() {
        return this.weekNumber;
    }

    public int getScore() {
        return this.score;
    }

    public Instant getCompletedAt() {
        return this.completedAt;
    }

    public int getTimeSpent() {
        return this.timeSpent;
    }

    public int getCorrectAnswers() {
        return this.correctAnswers;
    }

    public int getTotalQuestions() {
        return this.totalQuestions;
    }

    public String getAnswersJson() {
        return this.answersJson;
    }

    public void setStudent(final User student) {
        this.student = student;
    }

    public void setClassroom(final AppClassroom classroom) {
        this.classroom = classroom;
    }

    public void setTopic(final LearningTopic topic) {
        this.topic = topic;
    }

    public void setWeekNumber(final int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setScore(final int score) {
        this.score = score;
    }

    public void setCompletedAt(final Instant completedAt) {
        this.completedAt = completedAt;
    }

    public void setTimeSpent(final int timeSpent) {
        this.timeSpent = timeSpent;
    }

    public void setCorrectAnswers(final int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void setTotalQuestions(final int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public void setAnswersJson(final String answersJson) {
        this.answersJson = answersJson;
    }
}

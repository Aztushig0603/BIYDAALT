package com.flashcard;

import java.util.*;

public class AchievementTracker {
    private int correctStreak;
    private Map<String, Integer> answerAttempts;
    private Map<String, Integer> correctAnswers;

    public AchievementTracker() {
        this.correctStreak = 0;
        this.answerAttempts = new HashMap<>();
        this.correctAnswers = new HashMap<>();
    }

    public void recordAnswer(String question, boolean isCorrect) {
        answerAttempts.put(question, answerAttempts.getOrDefault(question, 0) + 1);
        if (isCorrect) {
            correctAnswers.put(question, correctAnswers.getOrDefault(question, 0) + 1);
            correctStreak++;
        } else {
            correctStreak = 0;
        }
    }

    public List<String> getAchievements() {
        List<String> achievements = new ArrayList<>();

        if (correctStreak > 0) {
            achievements.add("CORRECT: All cards were answered correctly in the last round");
        }
        for (Map.Entry<String, Integer> entry : answerAttempts.entrySet()) {
            if (entry.getValue() > 5) {
                achievements.add("REPEAT: Repeated " + entry.getKey() + " more than 5 times.");
            }
        }
        for (Map.Entry<String, Integer> entry : correctAnswers.entrySet()) {
            if (entry.getValue() >= 3) {
                achievements.add("CONFIDENT: Answered " + entry.getKey() + " correctly at least 3 times.");
            }
        }

        return achievements;
    }
}

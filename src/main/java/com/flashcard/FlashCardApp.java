package com.flashcard;

import java.io.*;
import java.util.*;

public class FlashCardApp {
    public static void main(String[] args) {
        if (args.length < 1 || "--help".equals(args[0])) {
            showHelp();
            return;
        }

        String filePath = args[0];
        Map<String, String> flashcards = loadFlashcards(filePath);
        if (flashcards.isEmpty()) {
            System.out.println("No flashcards found in the file.");
            return;
        }

        boolean invertCards = false;
        int repetitions = 1;
        String order = "random";

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--help":
                    showHelp();
                    return;
                case "--order":
                    if (i + 1 < args.length) {
                        order = args[++i];
                    }
                    break;
                case "--repetitions":
                    if (i + 1 < args.length) {
                        repetitions = Integer.parseInt(args[++i]);
                    }
                    break;
                case "--invertCards":
                    invertCards = true;
                    break;
                default:
                    System.out.println("Unknown option: " + args[i]);
                    return;
            }
        }

        startFlashcardSession(flashcards, order, repetitions, invertCards);
    }

    private static Map<String, String> loadFlashcards(String filePath) {
        Map<String, String> flashcards = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    flashcards.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        System.out.println("Loaded " + flashcards.size() + " flashcards.");
        return flashcards;
    }

    private static void startFlashcardSession(Map<String, String> flashcards, String order, int repetitions,
            boolean invertCards) {
        List<Map.Entry<String, String>> cardList = new ArrayList<>(flashcards.entrySet());
        LinkedList<String> recentMistakes = new LinkedList<>();
        Map<String, Integer> mistakeCounts = new HashMap<>();

        Scanner scanner = new Scanner(System.in);
        AchievementTracker achievementTracker = new AchievementTracker();

        int totalCorrect = 0;
        int totalWrong = 0;

        for (int i = 0; i < repetitions; i++) {
            if ("recent-mistakes-first".equals(order) && i > 0) {
                Set<String> seen = new HashSet<>();
                List<Map.Entry<String, String>> prioritized = new ArrayList<>();

                // Add recent mistakes first
                for (String key : recentMistakes) {
                    if (flashcards.containsKey(key) && !seen.contains(key)) {
                        prioritized.add(new AbstractMap.SimpleEntry<>(key, flashcards.get(key)));
                        seen.add(key);
                    }
                }

                // Add the rest of the cards
                for (Map.Entry<String, String> entry : flashcards.entrySet()) {
                    if (!seen.contains(entry.getKey())) {
                        prioritized.add(entry);
                        seen.add(entry.getKey());
                    }
                }

                cardList = prioritized;
            } else if ("worst-first".equals(order)) {
                if (i == 0) {
                    Collections.shuffle(cardList);
                } else {
                    CardOrganizer sorter = new WorstFirstSorter();
                    cardList = sorter.sort(flashcards, mistakeCounts);
                }
            } else if (!"recent-mistakes-first".equals(order)) {
                Collections.shuffle(cardList);
            }

            for (Map.Entry<String, String> entry : cardList) {
                String question = invertCards ? entry.getValue() : entry.getKey();
                String answer = invertCards ? entry.getKey() : entry.getValue();

                System.out.println("Question: " + question);
                System.out.print("Your answer: ");
                String userAnswer = scanner.nextLine().trim();

                boolean isCorrect = userAnswer.equalsIgnoreCase(answer);
                if (isCorrect) {
                    System.out.println("Correct!");
                    totalCorrect++;
                } else {
                    System.out.println("Wrong! The correct answer is: " + answer);
                    totalWrong++;

                    recentMistakes.remove(entry.getKey());
                    recentMistakes.addFirst(entry.getKey());
                    mistakeCounts.put(entry.getKey(), mistakeCounts.getOrDefault(entry.getKey(), 0) + 1);
                }

                System.out.println("Correct: " + totalCorrect + " | Wrong: " + totalWrong);
                System.out.println();

                achievementTracker.recordAnswer(question, isCorrect);
            }
        }

        List<String> achievements = achievementTracker.getAchievements();
        if (achievements.isEmpty()) {
            System.out.println("No achievements yet.");
        } else {
            System.out.println("\nAchievements:");
            for (String achievement : achievements) {
                System.out.println(achievement);
            }
        }

        System.out.println("\nFinal Stats:");
        System.out.println("Total Correct: " + totalCorrect);
        System.out.println("Total Wrong: " + totalWrong);

        scanner.close();
    }

    private static void showHelp() {
        System.out.println("Usage: flashcard <card-file> [options]");
        System.out.println("Options:");
        System.out.println(" --help               Show this help message");
        System.out.println(" --order <order>      Card order: random, worst-first, recent-mistakes-first");
        System.out.println(" --repetitions <num>  Number of times each card must be answered correctly");
        System.out.println(" --invertCards        Swap questions and answers");
    }
}

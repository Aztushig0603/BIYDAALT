package com.flashcard;

import java.util.*;

public class RecentMistakesFirstSorter implements CardOrganizer {
    @Override
    public List<Map.Entry<String, String>> sort(Map<String, String> flashcards, Map<String, Integer> mistakes) {
        List<Map.Entry<String, String>> sortedCards = new ArrayList<>(flashcards.entrySet());
        sortedCards.sort((a, b) -> mistakes.getOrDefault(b.getKey(), 0) - mistakes.getOrDefault(a.getKey(), 0));
        return sortedCards;
    }
}

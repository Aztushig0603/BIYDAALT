package com.flashcard;

import java.util.*;

public interface CardOrganizer {
    List<Map.Entry<String, String>> sort(Map<String, String> flashcards, Map<String, Integer> mistakes);
}

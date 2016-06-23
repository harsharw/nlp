package com.wishitventures.nlp.analytics;

import com.wishitventures.nlp.domain.Word;
import com.wishitventures.nlp.domain.WordScoresMap;
import com.wishitventures.nlp.domain.WordScoresMap.WordScore;

import java.util.*;

public final class WordStatsUtil {

    public static WordScoresMap wordCountsSorted(Collection<Word> words, String id) {
        Map<Word, Integer> wordToCount = wordCountsMap(words);
        List<WordScore> scores = new ArrayList<WordScore>();
        for(Word word: wordToCount.keySet()) {
            scores.add(new WordScore(word, wordToCount.get(word).doubleValue()));
        }
        return sortedDesc(scores, id);
    }

    public static Map<Word, Integer> wordCountsMap(Collection<Word> words) {
        Map<Word, Integer> wordToCount = new HashMap<Word, Integer>();
        for(Word word: words) {
            if(!wordToCount.containsKey(word)) {
                wordToCount.put(word, 0);
            }
            int previous = wordToCount.get(word);
            wordToCount.put(word, previous + 1);
        }
        return wordToCount;
    }

    public static WordScoresMap sortedDesc(List<WordScore> scores, String id) {
        Collections.sort(scores);
        Collections.reverse(scores);
        WordScoresMap toReturn = new WordScoresMap(id);
        toReturn.addAll(scores);
        return toReturn;
    }

}

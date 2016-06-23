package com.wishitventures.nlp.analytics;

import com.wishitventures.nlp.domain.Word;
import com.wishitventures.nlp.domain.WordScoresMap;

import java.util.HashSet;
import java.util.List;

public interface KeyWordExtractor {

    List<KeyWords> extract();

    public interface KeyWords {

        HashSet<KeyWord> keyWords();
        String id();

        public final static class KeyWord extends WordScoresMap.WordScore {
            private final int itemCount;
            private final int corpusCount;

            public KeyWord(Word word, double score, String id,
                           int itemCount, int corpusCount) {
                super(word, score, id);
                this.itemCount = itemCount;
                this.corpusCount = corpusCount;
            }

            @Override
            public boolean equals(Object o) {
                if(o instanceof KeyWord) {
                    return ((KeyWord)o).word().equals(word());
                }
               return this == o;
            }

            @Override
            public int hashCode() {
                return word().hashCode();
            }

            public int itemCount() {
                return itemCount;
            }

            public int corpusCount() {
                return corpusCount;
            }

            @Override
            public String toString() {
                return "KeyWord{" +
                        "word=" + word() +
                        "itemCount=" + itemCount +
                        ", corpusCount=" + corpusCount +
                        ", score=" + score() +
                        '}';
            }
        }
    }
}

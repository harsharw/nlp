package com.wishitventures.nlp.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

public final class WordScoresMap<T extends WordScoresMap.WordScore> {

    private final String id;
    private final LinkedHashMap<Word, T> wordScores = new LinkedHashMap<Word, T>();

    public WordScoresMap(String id) {
        this.id = id;
    }

    public void addAll(Collection<T> score) {
        for(T t: score) {
            add(t);
        }
    }

    public T add(T score) {
        return wordScores.put(score.word(), score);
    }

    public Set<Word> wordsSameOrder() {
        return wordScores.keySet();
    }

    public T score(Word word) {
        return wordScores.get(word);
    }

    public boolean contains(Word word) {
        return wordScores.containsKey(word);
    }

    @Override
    public String toString() {
        return wordScores.toString();
    }

    public static class WordScore implements Comparable<WordScore> {

        private final Word word;
        private final double score;
        private final String id;

        public WordScore(Word word, double score) {
            this.word = word;
            this.score = score;
            this.id = "NA";
        }

        public WordScore(Word word, double score, String id) {
            this.word = word;
            this.score = score;
            this.id = id;
        }

        public Word word() {
            return word;
        }

        public double score() {
            return score;
        }

        @Override
        public String toString() {
            return "WordScore{" +
                    "word=" + word +
                    ", score=" + score +
                    ", id='" + id + '\'' +
                    '}';
        }

        @Override
        public int compareTo(WordScore kw) {
            return this.score == kw.score ? 0 : this.score > kw.score ? 1 : -1;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof WordScore) {
                return ((WordScore)o).word().equals(word);
            }
            return this == o;
        }

        @Override
        public int hashCode() {
            return word.hashCode();
        }
    }
}

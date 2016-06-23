package com.wishitventures.nlp.analytics;

import com.wishitventures.nlp.domain.ItemText;
import com.wishitventures.nlp.domain.SemanticText;
import com.wishitventures.nlp.domain.Word;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import com.wishitventures.nlp.domain.SemanticText.Paragraph.Sentence;

import java.util.*;

public interface WordDistances {
    Map<Word, DistanceToWords> wordDistances(HashSet<Word> keyWordSet);

    interface DistanceToWords {

        List<Word> closestTargets();
    }

    final class DefaultWordDistances implements WordDistances {

        private final Collection<ItemText> texts;

        public DefaultWordDistances(Collection<ItemText> texts) {
            this.texts = texts;
        }

        @Override
        public Map<Word, DistanceToWords> wordDistances(HashSet<Word> keyWordSet) {
            Map<Word, DistanceToWords> distances = new HashMap<Word, DistanceToWords>();
            for (Word keyWord : keyWordSet) {
                distances.put(keyWord, new DefaultDistanceToWords(keyWord));
            }
            for (ItemText itemText : texts) {
                for (SemanticText txt : itemText.descriptions()) {
                    for (SemanticText.Paragraph para : txt.paragraphs()) {
                        for (Sentence sentence : para.sentences()) {
                            TIntArrayList indices = sentence.indices(keyWordSet);
                            if (indices.size() > 0) { // Sentence contains at least one keyword
                                populateDistancesToTargetWords(distances, sentence, indices);
                            }
                        }
                    }
                }
            }
            return distances;
        }

        private void populateDistancesToTargetWords(Map<Word, DistanceToWords> wordToWordDistances,
                                                    Sentence sentence, TIntArrayList targetIndices) {
            for (int i = 0; i < sentence.words().size(); i++) {
                Word word = sentence.words().get(i);

                DefaultDistanceToWords distances = (DefaultDistanceToWords)wordToWordDistances.get(word);
                if (wordToWordDistances.containsKey(word)) {
                    for (int j : targetIndices.toArray()) {
                        int abs = Math.abs(i - j);
                        if (abs > 0) {
                            distances.add(sentence.words().get(j), abs);
                        }
                    }
                }
            }
        }

        private static double avg(TIntArrayList values) {
            int sum = 0;
            for (int i : values.toArray()) {
                sum += i;
            }
            return (double) sum / values.size();
        }

        public static final class DefaultDistanceToWords implements DistanceToWords {
            final Word word;
            final Map<Word, TIntArrayList> distancesOfWordToTargetWords = new HashMap<Word, TIntArrayList>();

            DefaultDistanceToWords(Word word) {
                this.word = word;
            }

            void add(Word keyWord, int distance) {
                if (!distancesOfWordToTargetWords.containsKey(keyWord)) {
                    distancesOfWordToTargetWords.put(keyWord, new TIntArrayList());
                }
                distancesOfWordToTargetWords.get(keyWord).add(distance);
            }

            @Override
            public List<Word> closestTargets() {
                TDoubleObjectHashMap avgDistToWords = new TDoubleObjectHashMap();
                for (Word targetWords : distancesOfWordToTargetWords.keySet()) {
                    double avg = avg(distancesOfWordToTargetWords.get(targetWords));
                    avgDistToWords.put(avg, targetWords);
                }
                double[] distances = avgDistToWords.keySet().toArray().clone();
                Arrays.sort(distances);
                List<Word> toReturn = new ArrayList<Word>();
                for (double dist : distances) {
                    toReturn.add((Word) avgDistToWords.get(dist));
                }
                return toReturn;
            }
        }

    }
}

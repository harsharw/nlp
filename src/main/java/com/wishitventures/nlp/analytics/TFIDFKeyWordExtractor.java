package com.wishitventures.nlp.analytics;

import com.wishitventures.nlp.domain.*;

import java.util.*;

public final class TFIDFKeyWordExtractor implements KeyWordExtractor {

    private final Collection<ItemText> texts;
    private List<KeyWords> toReturn = null;

    public TFIDFKeyWordExtractor(Collection<ItemText> texts) {
        this.texts = texts;
    }

    @Override
    public List<KeyWords> extract() {
        if (toReturn != null) {
            return toReturn;
        }
        WordsCollectionFactory.WordsList allTexts = new WordsCollectionFactory.WordsList();

        for (ItemText itemText : texts) {
            for (SemanticText text : itemText.descriptions()) {
                allTexts.addAll(text.words());
            }
        }
        WordScoresMap countsAllTexts = wordCounts(allTexts.words(), "NA");

        toReturn = new ArrayList<KeyWords>();

        for (ItemText itemText : texts) {
            String id = itemText.id();
            toReturn.add(new TFIDFKeyWords(countsAllTexts,
                    wordCounts(itemText.words(), id), id));
        }
        return toReturn;
    }

    private WordScoresMap wordCounts(List<Word> words, String id) {
        return WordStatsUtil.wordCountsSorted(words, id);
    }

    /**
     * Keywords for one item
     */
    public static final class TFIDFKeyWords implements KeyWords {
        private final WordScoresMap forAllTexts;
        private final WordScoresMap forText;
        private final String id;
        private HashSet<KeyWords.KeyWord> toReturn = null;

        public TFIDFKeyWords(WordScoresMap forAllTexts, WordScoresMap forText, String id) {
            this.forAllTexts = forAllTexts;
            this.forText = forText;
            this.id = id;
        }

        @Override
        public HashSet<KeyWord> keyWords() {
            if (toReturn != null) {
                return toReturn;
            }
            List<KeyWords.KeyWord> wordScoresList = new ArrayList<KeyWords.KeyWord>();
            for (Object o : forText.wordsSameOrder()) {
                Word word = (Word) o;
                double countForAllTexts = forAllTexts.score(word).score();
                double countForText;

                WordScoresMap.WordScore wordCount = forText.score(word);
                countForText = wordCount.score();
                double score = countForText / countForAllTexts;
                wordScoresList.add(new KeyWords.KeyWord(word, score, id,
                        (int) countForText, (int) countForAllTexts));

            }
            Collections.sort(wordScoresList);
            Collections.reverse(wordScoresList);
            toReturn = new LinkedHashSet<KeyWord>(wordScoresList); // Setting lazy load var
            return toReturn;
        }

        @Override
        public String id() {
            return id;
        }

    }

}

package com.wishitventures.nlp.domain;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SemanticText implements WordsCollectionFactory.WordsCollector {

    private final Type type;
    private final String id;

    public SemanticText(String contents, Dictionary dictionary, Type type, String id) {
        this.type = type;
        this.id = id;
        for (String paraTxt : paragraphStr(contents)) {
            this.paragraphs.add(new Paragraph(paraTxt, dictionary));
        }
    }

    public String id() {
        return id;
    }

    public static String[] paragraphStr(String contents) {
        return contents.split("[\\r\\n]+");
    }

    public List<Paragraph> paragraphs() {
        return Collections.unmodifiableList(paragraphs);
    }

    @Override
    public List<Word> words() {
        List<Word> words = new ArrayList<Word>();
        for(Paragraph para: paragraphs) {
            words.addAll(para.words());
        }
        return words;
    }

    private final List<Paragraph> paragraphs = new ArrayList<Paragraph>();

    public enum Type {
        UserReview, EditorialReview, Summary, Contents
    }

    // TODO: Optimize storage of words using int references
    public static final class Paragraph implements WordsCollectionFactory.WordsCollector {

        private final List<Sentence> sentences = new ArrayList<Sentence>();

        public Paragraph(String paraText, Dictionary dictionary) {
            for (String sentence : splitIntoSentences(paraText)) {
                this.sentences.add(new Sentence(sentence, dictionary));
            }
        }

        public static String[] splitIntoSentences(String paraText) {
            return StringUtils.split(paraText, ".");
        }

        public List<Sentence> sentences() {
            return Collections.unmodifiableList(this.sentences);
        }

        @Override
        public List<Word> words() {
            List<Word> words = new ArrayList<Word>();
            for(Sentence sentence: sentences) {
                words.addAll(sentence.words());
            }
            return words;
        }

        public static final class Sentence extends WordsCollectionFactory.WordsList {

            public Sentence(String text, Dictionary dictionary) {
                for (String value : textWords(text)) {
                    Word.BaseWord word = EnglishWord.inferType(value, dictionary);
                    if (word != null) {
                        super.add(word);
                    }
                }
            }

            static String[] textWords(String text) {
                return text.split("\\s+");
            }
        }
    }

}

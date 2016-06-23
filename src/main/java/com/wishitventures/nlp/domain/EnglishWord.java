package com.wishitventures.nlp.domain;
import com.wishitventures.nlp.domain.EnglishWord.EnglishWordProps.PartOfSpeech;

import java.util.HashSet;
import java.util.Set;

public final class EnglishWord extends Word.BaseWord {

    private final String characters;
    private final Set<PartOfSpeech> partOfSpeech = new HashSet<>();

    EnglishWord(String rawValue, PartOfSpeech partOfSpeech) {
        super(rawValue);
        this.characters = rawValue;//onlyLetters(rawValue);
        this.partOfSpeech.add(partOfSpeech);
    }

    public static EnglishWord fromStr(String rawValue) {
        return new EnglishWord(rawValue, null);
    }

    public static EnglishWord fromStr(String rawValue, PartOfSpeech partOfSpeech) {
        EnglishWord word = new EnglishWord(rawValue, partOfSpeech);
        return word.characters().isEmpty() ? null : word;
    }

  public static void tagSynonyms(EnglishWord word1, EnglishWord word2) {
    if (!word1.synonyms().contains(word2)) {
      word1.addSynonym(word2);
    }
    if (!word2.synonyms().contains(word1)) {
      word2.addSynonym(word1);
    }
  }

  public static void tagVariants(EnglishWord word1, EnglishWord word2) {
    if (!word1.variants().contains(word2)) {
      word1.addVariant(word2);
    }
    if (!word2.variants().contains(word1)) {
      word2.addVariant(word1);
    }
  }

  public boolean addPartOfSpeech(PartOfSpeech pos) {
      return partOfSpeech.add(pos);
    }

    public static EnglishWord inferType(String rawValue, Dictionary dictionary) {
        String toLowerCase = rawValue.toLowerCase();
        String chars = toLowerCase; //onlyLetters(toLowerCase);
        if (dictionary.contains(chars)) {
            return (EnglishWord)dictionary.fromChars(chars);
        }
        return fromStr(toLowerCase, PartOfSpeech.Unknown);
    }

    @Override
    public String characters() {
        return characters;
    }

    public Set<PartOfSpeech> partOfSpeech() {
        return partOfSpeech;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof EnglishWord) {
            return characters().equals(((EnglishWord)o).characters());
        }
        return this == o;
    }

    @Override
    public int hashCode() {
        return characters().hashCode();
    }

    @Override
    public String toString() {
        return rawValue() + "(" + partOfSpeech + ")";
    }

    public static final class EnglishWordProps {

        private final PartOfSpeech partOfSpeech;

        public EnglishWordProps(PartOfSpeech partOfSpeech) {
            this.partOfSpeech = partOfSpeech;
        }

        public enum PartOfSpeech {
            Noun, Verb, Adjective, ProNoun, Adverb, Preposition, Conjunction, Determiner, Interjection, Unknown,
            AdjectiveSatellite
        }
   }
}

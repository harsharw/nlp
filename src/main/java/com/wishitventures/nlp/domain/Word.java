package com.wishitventures.nlp.domain;

import java.util.HashSet;
import java.util.LinkedHashSet;
import org.tartarus.snowball.ext.englishStemmer;

public interface Word {
  String characters();

  String rawValue();

  HashSet<Word> synonyms();

  HashSet<Word> variants();

  public abstract class BaseWord implements Word {

    private final String rawValue;

    private final HashSet<Word> synonyms = new HashSet<Word>();
    private final HashSet<Word> variants = new LinkedHashSet<Word>();

    protected BaseWord(String rawValue) {
      this.rawValue = rawValue;
    }

    @Override
    public HashSet<Word> variants() {
      return variants;
    }

    @Override
    public HashSet<Word> synonyms() {
      return synonyms;
    }

    public HashSet<Word> synonymsTarget(int targetNum) {
      HashSet<Word> targetSynonyms = new LinkedHashSet<>();
      for(Word synonym: synonyms) {
        targetSynonyms.add(synonym);
        if(targetSynonyms.size() < targetNum) {
          targetSynonyms.addAll(synonym.synonyms());
        }
      }
      return targetSynonyms;
    }

    public boolean addSynonym(EnglishWord synonym) {
      return synonyms.add(synonym);
    }

    public boolean addVariant(EnglishWord ew) {
      return variants.add(ew);
    }

    public static String onlyLetters(String rawValue) {
      return rawValue.replaceAll("[^a-zA-Z]+", "");
    }

    public static String stemmed(String rawValue) {
      englishStemmer stemmer = new englishStemmer();
      stemmer.setCurrent(rawValue);
      if(stemmer.stem()) {
        return stemmer.getCurrent();
      }
      return "";
    }

    @Override
    public String rawValue() {
      return rawValue;
    }
  }
}

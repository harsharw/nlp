package com.wishitventures.nlp.domain;

import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public final class WordsCollectionFactory {
  public static interface WordsCollection extends WordsCollector {

    void add(Word word);

    void addAll(Collection<? extends Word> words);
  }

  public static final class TextToWordsMap implements WordsCollection {

    private final Map<String, Word> rawValueToWord = new HashMap<>();

    public Word get(String rawTxt) {
      return rawValueToWord.get(rawTxt.trim());
    }

    @Override
    public void add(Word word) {
      rawValueToWord.put(word.rawValue(), word);
    }

    public void addAll(Collection<? extends Word> words) {
      for (Word word : words) {
        String rawValue = word.rawValue();
        EnglishWord ew = (EnglishWord) word;
        if (!rawValueToWord.containsKey(rawValue)) {
          rawValueToWord.put(rawValue, word);
        } else {
          EnglishWord englishWord  = (EnglishWord)rawValueToWord.get(rawValue);
          ew.addPartOfSpeech(englishWord.partOfSpeech().iterator().next());
        }
      }
    }

    @Override
    public Collection<Word> words() {
      return rawValueToWord.values();
    }

    public boolean contains(String rawText) {
      return rawValueToWord.containsKey(rawText);
    }
  }

  public static class WordsList implements WordsCollection {

    private final List<Word> words = new ArrayList<Word>();

    @Override
    public void add(Word word) {
      this.words.add(word);
    }

    @Override
    public void addAll(Collection<? extends Word> words) {
      this.words.addAll(words);
    }

    @Override
    public List<Word> words() {
      return Collections.unmodifiableList(words);
    }

    public Word atIndex(int ind) {
      return words.get(ind);
    }

    public boolean contains(Word word) {
      return words.contains(word);
    }

    public TIntArrayList indices(HashSet<Word> words) {
      TIntArrayList indices = new TIntArrayList();
      for (int i = 0; i < words().size(); i++) {
        if (words.contains(words().get(i))) {
          indices.add(i);
        }
      }
      return indices;
    }

    public List<Word> window(int index, int before, int after) {
      int start = (index - before > 0) ? index - before : 0;
      int end = (index + after < words.size() - 1) ? index + after : words.size() - 1;
      return words.subList(start, end + 1);
    }

    public String windowText(int index, int before, int after) {
      List<Word> window = window(index, before, after);
      return StringUtils.join(window, " ");
    }

    @Override
    public String toString() {
      return "WordsList{" +
          "wordsSameOrder=" + words +
          '}';
    }
  }

  public static interface WordsCollector {
    Collection<Word> words();
  }
}

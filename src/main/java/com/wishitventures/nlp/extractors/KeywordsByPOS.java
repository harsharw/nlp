package com.wishitventures.nlp.extractors;

import com.wishitventures.nlp.domain.EnglishWord;
import com.wishitventures.nlp.domain.WordsCollectionFactory;
import com.wishitventures.nlp.rules.ProtoType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class KeywordsByPOS {

  private static final String ER_PATH = "/Users/ECA/Desktop/db_dump/EditorialReview_Content_04_30_16.tag";
  public static final String[] PROPER_NOUN = new String[]{"nn", "vb", "jj"};  // Leave out the '_'

  public static void main(String[] args) throws IOException {
    KeywordsByPOS keywordsByPOS = new KeywordsByPOS();
    keywordsByPOS.genKeyWords();

  }

  private void genKeyWords() throws IOException {
    // Calculate term counts
    List<WordCountsByID> termCounts = new ArrayList<>();
    populateTermCounts(termCounts);

    // Calc document frequencies
    Map<String, Integer> documentFrequencies = new HashMap<>();
    computeInverseDocFrequencies(termCounts, documentFrequencies);

    // Write keywords
    writeKeywords(termCounts, documentFrequencies);
  }

  private void writeKeywords(List<WordCountsByID> termCounts,
                             Map<String, Integer> documentFrequencies) throws FileNotFoundException {
    PrintWriter writer = new PrintWriter("/Users/ECA/Desktop/db_dump/keywords/keywords_04_30_16_nn_vb_jj_2.txt");
    int numDocs = termCounts.size();

    for(WordCountsByID byID: termCounts) {
      List<WordScore> wordScores = new ArrayList<>();
      for(WordCount wordCount: byID.wordCounts.values()) {
        String word = wordCount.word;
        double tf = ((double) wordCount.getCount()) / byID.getNumWords();
        double idf =  Math.log(numDocs / documentFrequencies.get(word));

        double score = tf * idf;

        wordScores.add(new WordScore(word, score));
      }

      Collections.sort(wordScores);

      for(WordScore wordScore: wordScores) {
        String outStr = byID.id + "\t" + wordScore.word + "\t" + wordScore.score + "\n";
        writer.write(outStr);
        writer.flush();
      }
    }
  }

  private void computeInverseDocFrequencies(List<WordCountsByID> termCounts,
                                            Map<String, Integer> documentFrequencies) {
    for(WordCountsByID byID: termCounts) {
      for(WordCount wordCount: byID.wordCounts.values()) {
        if(!documentFrequencies.containsKey(wordCount.word)) {
          documentFrequencies.put(wordCount.word, 0);
        }
      }
    }

    for(String word: documentFrequencies.keySet()) {
      for(WordCountsByID byID: termCounts) {
        if(byID.wordCounts.containsKey(word)) {
          int previous = documentFrequencies.get(word);
          documentFrequencies.put(word, previous + 1);
        }
      }
    }
  }

  private void populateTermCounts(List<WordCountsByID> termCounts) throws IOException {
    WordsCollectionFactory.WordsList stopWords = ProtoType.stopWords();

    List<String> lines = FileUtils.readLines(new File(ER_PATH));
    for(String line: lines) {
      String[] words = StringUtils.split(clean(line), " ");
      String id = StringUtils.substringBeforeLast(words[0], "_");

      WordCountsByID byID = new WordCountsByID(id);

      if(words.length > 1) {
        processSingleKeyWord(stopWords, words, byID);
        //processPhrase(stopWords, words, byID);
      }
      termCounts.add(byID);
    }
  }

  private void processSingleKeyWord(WordsCollectionFactory.WordsList stopWords,
                                    String[] words, WordCountsByID byID) {
    for(int j = 1; j < words.length; j++) {
      String word = words[j].trim().toLowerCase(); // supernatural_jj
      String[] pieces = StringUtils.split(word, "_");

      if(pieces != null && pieces.length > 1) {
        String targetPOS = pieces[1];
        String wordStr = pieces[0];

        for(String pos: PROPER_NOUN) {
          if(targetPOS.contains(pos) && wordStr.length() > 2
              && !stopWords.contains(EnglishWord.fromStr(wordStr))) {
            byID.add(wordStr);
          }
        }
      }
    }
    byID.setNumWords(words.length);
  }

  private void processPhrase(WordsCollectionFactory.WordsList stopWords,
                                    String[] words, WordCountsByID byID) {

    if(words.length > 1) {
      for(int i = 1; i < words.length; i++) {

      }
    }
  }


  private static class WordScore implements Comparable<WordScore>{

    private final String word;
    private final double score;

    private WordScore(String word, double score) {
      this.word = word;
      this.score = score;
    }

    @Override
    public int compareTo(WordScore o) {
      if(score > o.score) {
        return -1;
      } else if(score < o.score) {
        return 1;
      }
      return 0;
    }
  }

  private static String clean(String str) {
    return str.replace(".", ". ").replace("?", "? ").replace("!", "! ");
  }

  private static final class WordCountsByID {
    private final String id;
    private int numWords = 0;

    Map<String, WordCount> wordCounts = new HashMap<>();

    private WordCountsByID(String id) {
      this.id = id;
    }

    void add(String word) {
      if(!wordCounts.containsKey(word)) {
        wordCounts.put(word, new WordCount(word));
      } else {
        wordCounts.get(word).incrementCount();
      }
    }

    private int getNumWords() {
      return numWords;
    }

    private void setNumWords(int numWords) {
      this.numWords = numWords;
    }
  }

  private static final class WordCount {

    private int count = 1;
    final String word;

    private WordCount(String word) {
      this.word = word;
    }

    void incrementCount() {
      count++;
    }

    public int getCount() {
      return count;
    }
  }
}

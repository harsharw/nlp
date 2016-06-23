package com.wishitventures.nlp.rules;


import com.wishitventures.nlp.domain.EnglishWord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class TFIDFPhrase {

  public static void main(String[] args) throws IOException {
    extractPhrases();
  }

  private static void test() throws IOException {
    List<String> lines = FileUtils.readLines(new File("/Users/ECA/Desktop/db_dump/keyphrases/key_phrase_scores.txt"));

    Set<String> nouns = new HashSet<>();
    Set<String> adjectives = new HashSet<>();
    Set<String> phrases = new HashSet<>();

    for(String line: lines) {
      String[] fields = StringUtils.split(line, "\t");
      String[] phrase = fields[1].split(" ");
      String noun = phrase[1];
      String adjective = phrase[0];
      String stemmedNoun = EnglishWord.stemmed(noun);
      String stemmedAdjective = EnglishWord.stemmed(adjective);

      nouns.add(noun);
      adjectives.add(stemmedAdjective);
      phrases.add(fields[1]);
    }

    for(String phrase: phrases) {
      System.out.println(phrase);
    }
  }

  private static void extractPhrases() throws IOException {
    PhraseExtraction phraseExtraction = new PhraseExtraction();
    List<HashMap<String, PhraseExtraction.PhraseCount>> eachDoc = phraseExtraction.extractPhraseCountsForEachDoc();
    LinkedHashMap<String, List<String>> inHashMapForm = docIDsToPhrases(eachDoc);

    List<List<String>> inTFIDFForm = new ArrayList<>();
    for(List<String> doc: inHashMapForm.values()) {
      inTFIDFForm.add(doc);
    }

    Iterator<String> iterator = inHashMapForm.keySet().iterator();

    PrintWriter writer = new PrintWriter(new File("/Users/ECA/Desktop/db_dump/keyphrases/key_phrase_scores_04_14_16.txt"));

    for(List<String> doc: inTFIDFForm) {
      String asin = iterator.next();
      List<KeyPhraseScore> scores = new ArrayList<>();

      for(String term: doc) {
        double v = tfIdf(doc, inTFIDFForm, term);
        scores.add(new KeyPhraseScore(term, v));
        //System.out.println("[" + asin + "] " + term + " <--> " + v);
      }

      Collections.sort(scores);
      //System.out.println(asin + " --> " + subList(scores));
      for(KeyPhraseScore score: scores) {
        writer.print(asin + "\t" + score.phrase + "\t" + score.score + "\n");
        writer.flush();
      }
    }
    writer.close();
  }

  private static List<KeyPhraseScore> subList(List<KeyPhraseScore> scores) {
    List<KeyPhraseScore> subList = new ArrayList<>();
    int i = 0;
    for(KeyPhraseScore score: scores) {
      //if(i > 10) break;

      subList.add(score);

      i++;
    }

    return subList;
  }

  private static final class KeyPhraseScore implements Comparable<KeyPhraseScore> {
    private final String phrase;
    private final double score;

    private KeyPhraseScore(String phrase, double score) {
      this.phrase = phrase;
      this.score = score;
    }

    @Override
    public int compareTo(KeyPhraseScore o) {
      if(score > o.score) {
        return -1;
      }
      if(score < o.score) {
        return 1;
      }
      return 0;
    }

    @Override
    public String toString() {
      return phrase + " [" + score + "]";
    }
  }

  private static LinkedHashMap<String, List<String>> docIDsToPhrases(
      List<HashMap<String, PhraseExtraction.PhraseCount>> eachDoc) {

    LinkedHashMap<String, List<String>> toReturn = new LinkedHashMap<>();

    for(HashMap<String, PhraseExtraction.PhraseCount> pc: eachDoc) {
      for(PhraseExtraction.PhraseCount phraseCount: pc.values()) {
        toReturn.put(phraseCount.getDocumentID(), new ArrayList<>());
      }
    }

    for(HashMap<String, PhraseExtraction.PhraseCount> pc: eachDoc) {
      for(PhraseExtraction.PhraseCount phraseCount: pc.values()) {
        for(int i = 0; i < phraseCount.getCount(); i++) {
          toReturn.get(phraseCount.getDocumentID()).add(phraseCount.getPhrase());
        }
      }
    }
    return toReturn;
  }

  private static double idf(List<List<String>> docs, String term) {
    double n = 0;
    for (List<String> doc : docs) {
      for (String word : doc) {
        if (term.equalsIgnoreCase(word)) {
          n++;
          break;
        }
      }
    }
    return Math.log(docs.size() / n);
  }

  private static double tfIdf(List<String> doc, List<List<String>> docs, String term) {
    return tf(doc, term) * idf(docs, term);
  }

  private static double tf(List<String> doc, String term) {
    double result = 0;
    for (String word : doc) {
      if (term.equalsIgnoreCase(word))
        result++;
    }
    return result / doc.size();
  }
}

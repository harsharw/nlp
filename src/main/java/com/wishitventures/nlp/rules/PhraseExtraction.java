package com.wishitventures.nlp.rules;

import com.wishitventures.nlp.domain.*;
import com.wishitventures.nlp.domain.Dictionary;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public final class PhraseExtraction {

  private static WordsCollectionFactory.WordsList STOPWORDS = stopWords();
  private final static String STOPWORDS_PATH = "/Users/ECA/Desktop/Recommendation_Systems/Software/kea-5.0_full/data/stopwords/stopwords_en.txt";
  private final Dictionary dictionary;

  public PhraseExtraction() {
    this.dictionary = Dictionary.instance();
  }

  public static HashMap<String, Integer> aggregatePhraseCountsForAllDocs
      (List<HashMap<String, PhraseCount>> forEachDoc) throws FileNotFoundException {

    HashMap<String, Integer> totalPhraseCounts = new HashMap<>();

    for(HashMap<String, PhraseCount> eachDoc: forEachDoc) {
      for(PhraseCount phraseCount: eachDoc.values()) {
        String phrase = phraseCount.phrase;
        if(!totalPhraseCounts.containsKey(phrase)) {
          totalPhraseCounts.put(phrase, phraseCount.count);
        } else {
          Integer previous = totalPhraseCounts.get(phrase);
          totalPhraseCounts.put(phrase, previous + phraseCount.count);
        }
      }
    }

    PrintWriter writer = new PrintWriter("/Users/ECA/Desktop/db_dump/keyphrases/phrase_counts_04_14_16.txt");
    for(String phrase: totalPhraseCounts.keySet()) {
      writer.print(phrase + "\t" + totalPhraseCounts.get(phrase) + "\n");
      writer.flush();
    }
    writer.close();

    return totalPhraseCounts;
  }

  public List<HashMap<String, PhraseCount>> extractPhraseCountsForEachDoc() throws IOException {

    List<HashMap<String, PhraseCount>> phraseCountsAllDocs = new ArrayList<>();

    for(String line: readEditorialReview()) {
      String[] fields = StringUtils.split(line, "\t");
      if(fields.length < 2) {
        continue;
      }
      SemanticText semanticText = new SemanticText(fields[1], dictionary,
          SemanticText.Type.Contents, fields[0]);

      for(SemanticText.Paragraph p: semanticText.paragraphs()) {

        for(SemanticText.Paragraph.Sentence sentence: p.sentences()) {
          HashSet<Word> target = new HashSet<>();
          for(Word word: sentence.words()) {
            EnglishWord englishWord = (EnglishWord) word;

            if(englishWord.partOfSpeech().contains(EnglishWord.EnglishWordProps.PartOfSpeech.Noun)
                && !STOPWORDS.contains(englishWord)) {
              target.add(word);
            }
          }
          HashMap<String, PhraseCount> phraseCounts = phraseCountsForDoc(fields[0], target, sentence);
          if(!phraseCounts.isEmpty()) {
            phraseCountsAllDocs.add(phraseCounts);
            for(PhraseCount phraseCount: phraseCounts.values()) {
              //writer.print(phraseCount.documentID + "\t" + phraseCount.phrase
              //    + "\t" + phraseCount.count + "\n");
            }
          }
        }
      }

      //docIDs.add()
    }
    return phraseCountsAllDocs;
  }

  private HashMap<String, PhraseCount> phraseCountsForDoc(String id, HashSet<Word> target,
                                                             SemanticText.Paragraph.Sentence sentence) {
    TIntArrayList indices = sentence.indices(target);
    HashMap<String, PhraseCount> toReturn = new HashMap<>(); // Phrase for the doc

    for(int i : indices.toArray()) {
      List<Word> window = sentence.window(i, 1, 0);
      for(Word w: window) {
        EnglishWord englishWord = (EnglishWord)w;
        if(englishWord.partOfSpeech().contains(EnglishWord.EnglishWordProps.PartOfSpeech.Adjective)
            || englishWord.partOfSpeech().contains(EnglishWord.EnglishWordProps.PartOfSpeech.AdjectiveSatellite)) {
          String concat = new String(stemmed(window.get(0).rawValue())
              + " " + stemmed(window.get(1).rawValue()));
          if(!toReturn.containsKey(concat)) {
            toReturn.put(concat, new PhraseCount(id, concat));
          } else {
            toReturn.get(concat).incrementCount();
          }
        }
      }
    }
    return toReturn;
  }

  private static String stemmed(String rawValue) {
    return rawValue.toLowerCase().trim();
  }

  public static final class PhraseCount {
    private int count;
    private final String documentID;
    private final String phrase;

    private PhraseCount(String documentID, String phrase) {
      this.documentID = documentID;
      this.phrase = phrase;
      this.count = 1;
    }

    private int incrementCount() {
      return count++;
    }

    public int getCount() {
      return count;
    }

    public String getDocumentID() {
      return documentID;
    }

    public String getPhrase() {
      return phrase;
    }

    @Override
    public String toString() {
      return "PhraseCount{" +
          "count=" + count +
          ", documentID='" + documentID + '\'' +
          ", phrase='" + phrase + '\'' +
          '}';
    }
  }


  public static List<String> readEditorialReview() throws IOException {
    return FileUtils.readLines(new File("/Users/ECA/Desktop/db_dump/EditorialReview_Content_04_13_16.tsv"));
  }

  private static WordsCollectionFactory.WordsList stopWords() {
    WordsCollectionFactory.WordsList stopWords = new WordsCollectionFactory.WordsList();
    List<String> stopWordsStr = null;
    try {
      stopWordsStr = FileUtils.readLines(new File(STOPWORDS_PATH));
    } catch (IOException e) {
      e.printStackTrace();
    }
    for(String stopWord: stopWordsStr) {
      stopWords.add(EnglishWord.fromStr(stopWord));
    }
    return stopWords;
  }

}

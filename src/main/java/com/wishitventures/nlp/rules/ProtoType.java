package com.wishitventures.nlp.rules;

import com.wishitventures.nlp.domain.*;
import com.wishitventures.nlp.domain.Dictionary;
import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class ProtoType {

  private final static String STOPWORDS_PATH = "/Users/ECA/Desktop/Recommendation_Systems/Software/kea-5.0_full/data/stopwords/stopwords_en.txt";

  private static WordsCollectionFactory.WordsList STOPWORDS = stopWords();

  private static void writePhraseCounts() throws IOException {
    BasicConfigurator.configure();
    //WordSenseDictionaryLoader loader =
    //    new WordSenseDictionaryLoader(new File(WORDSENSE_DICTIONARY_PATH));
    //TextToWordsMap dictionaryWords = (TextToWordsMap) loader.loadWords();
    List<String> lines = PhraseExtraction.readEditorialReview();

    Dictionary dictionary = Dictionary.instance();

    EnglishWord targetWord = EnglishWord.fromStr("war");

    HashMap<String, Integer> counts = new HashMap<>();

    for(String content: lines) {
      String[] fields = StringUtils.split(content, "\t");
      if(fields.length < 2) {
        continue;
      }

      SemanticText semanticText = new SemanticText(fields[1], dictionary,
          SemanticText.Type.Contents, "1");

      List<Word> words = new ArrayList<>();

      for(Word word: semanticText.words()) {
        EnglishWord englishWord = (EnglishWord)word;
        if(!englishWord.partOfSpeech().isEmpty() &&
            EnglishWord.EnglishWordProps.PartOfSpeech.Noun
                == englishWord.partOfSpeech().iterator().next()) {
          words.add(englishWord);
        }
      }

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
          HashMap<String, Integer> item = printWindow(fields[0], target, sentence);
          if(!item.isEmpty()) {
            add(counts, item);
          }
        }
      }
    }

    Map<Integer, Set<String>> countsToTerms = new HashMap<>();

    for(String term: counts.keySet()) {
      Integer count = counts.get(term);
      if(!countsToTerms.containsKey(count)) {
        countsToTerms.put(count, new HashSet<>());
      }
      countsToTerms.get(count).add(term);
    }

    List<Integer> termCounts = new ArrayList<>();
    termCounts.addAll(countsToTerms.keySet());

    Collections.sort(termCounts);
    Collections.reverse(termCounts);


    PrintWriter writer = new PrintWriter("/Users/ECA/Desktop/db_dump/keyphrases/phrase_counts_04_14_16.txt");

    for(Integer count: termCounts) {
      Set<String> terms = countsToTerms.get(count);

      for(String term: terms) {
        writer.println(count + "\t" + term);
        writer.flush();
      }
    }

    writer.close();
  }

  private static HashMap<String, Integer> printWindow(String id, HashSet<Word> target,
                                                      SemanticText.Paragraph.Sentence sentence) {
    TIntArrayList indices = sentence.indices(target);
    HashMap<String, Integer> toReturn = new HashMap<>();

    for(int i : indices.toArray()) {
      List<Word> window = sentence.window(i, 1, 0);
      for(Word w: window) {
        EnglishWord englishWord = (EnglishWord)w;
        if(englishWord.partOfSpeech().contains(EnglishWord.EnglishWordProps.PartOfSpeech.Adjective)
            || englishWord.partOfSpeech().contains(EnglishWord.EnglishWordProps.PartOfSpeech.AdjectiveSatellite)) {
          //System.out.println(id + " <--> " + window);
          String concat = new String(window.get(0).rawValue() + " " + window.get(1).rawValue()).toLowerCase().trim();
          if(!toReturn.containsKey(concat)) {
            toReturn.put(concat, 1);
          } else {
            int previous = toReturn.get(concat);
            toReturn.put(concat, previous + 1);
          }
        }
      }
    }
    return toReturn;
  }

  public static WordsCollectionFactory.WordsList stopWords() {
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

  private static void add(HashMap<String, Integer> total, HashMap<String, Integer> item) {
    for(String key: item.keySet()) {
      if(!total.containsKey(key)) {
        total.put(key, item.get(key));
      } else {
        total.put(key, total.get(key) + item.get(key));
      }
    }
  }


}

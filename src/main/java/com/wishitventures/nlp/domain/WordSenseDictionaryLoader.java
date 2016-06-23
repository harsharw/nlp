package com.wishitventures.nlp.domain;

import org.apache.commons.lang.StringUtils;
import com.wishitventures.nlp.domain.EnglishWord.EnglishWordProps.PartOfSpeech;
import com.wishitventures.nlp.domain.WordsCollectionFactory.TextToWordsMap;
import org.apache.log4j.Logger;

import java.io.*;

import java.util.*;

public final class WordSenseDictionaryLoader implements Dictionary.DictionaryLoader {
  public static final String FP_WORKING_DIR = "/Users/ECA/Desktop/FP_Projects/FP_WorkingDir/";
  public static final String WORDSENSE_DICTIONARY_PATH = "/Users/ECA/Desktop/AWS_Search/Dictionaries/WordSense_3.0/";
  private final File configDir;

  private final static Logger logger = Logger.getLogger(WordSenseDictionaryLoader.class);

  public WordSenseDictionaryLoader(File configDir) {
    this.configDir = configDir;
  }

  @Override
  public WordsCollectionFactory.WordsCollector loadWords() throws IOException {
    TextToWordsMap words = loadDataWords();
    insertSynonyms(words);
    return words;
  }

  private void insertSynonyms(TextToWordsMap words) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(new File(configDir, "index.sense")));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] senseKeyParts = line.split("\\s")[0].split(":");
      if (senseKeyParts.length > 3) {
        String word1Txt = StringUtils.substringBefore(senseKeyParts[0], "%");
        String word2Txt = senseKeyParts[3];
        if (words.contains(word1Txt) && words.contains(word2Txt)) {
          EnglishWord.tagSynonyms((EnglishWord) words.get(word1Txt),
              (EnglishWord) words.get(word2Txt));
        }
      }
    }
    logger.info("Loaded synonyms");
  }

  private TextToWordsMap loadDataWords() throws IOException {
    TextToWordsMap textToWords = new TextToWordsMap();

    for (File file : configDir.listFiles()) {
      if (file.getName().contains("data")) {

        BufferedReader bf = new BufferedReader(new FileReader(file));
        String line;
        int lineNum = 0;
        while ((line = bf.readLine()) != null) {
          if (lineNum > 29) {
            String withoutDefinition = StringUtils.substringBefore(line, "|"); // After '|' is definition
            textToWords.addAll(englishWords(withoutDefinition));
          }
          lineNum++;
          if (lineNum % 10000 == 0) {
            logger.info("Loaded " + lineNum + " lines from " + file.getAbsolutePath());
          }
        }
      }
    }
    return textToWords;
  }

  private HashSet<EnglishWord> englishWords(String beforeDefinition) {
    String[] pieces = beforeDefinition.split("\\s");
    String type = pieces[2];
    char typeKey = type.toCharArray()[0]; // Converting to char just for the switch statement
    HashSet<String> variants = variants(pieces);
    LinkedHashSet<EnglishWord> variantWords = new LinkedHashSet<>();
    for (String variant : variants) {
      if (variant.contains("_")) {
        EnglishWord hyphenated = new EnglishWord(variant.replace("_", "-"),
            fromWordSenseKey(typeKey));
        EnglishWord spaceSeparated = new EnglishWord(variant.replace("_", " "),
            fromWordSenseKey(typeKey));
        hyphenated.addVariant(spaceSeparated);
        spaceSeparated.addVariant(hyphenated);
        variantWords.add(hyphenated);
        variantWords.add(spaceSeparated);
      } else {
        variantWords.add(new EnglishWord(variant, fromWordSenseKey(typeKey)));
      }
    }
    return variantWords;
  }

  private LinkedHashSet<String> variants(String[] strings) {
    LinkedHashSet<String> variants = new LinkedHashSet<>();
    for (String variant : strings) {
      String letters = Word.BaseWord.onlyLetters(variant);
      if (letters.length() > 2) {
        variants.add(letters);
      }
    }
    return variants;
  }

  public static PartOfSpeech fromWordSenseKey(char key) {
    PartOfSpeech toReturn = null;
    switch (key) {
      case 'a':
        toReturn = PartOfSpeech.Adjective;
        break;
      case 's':
        toReturn = PartOfSpeech.AdjectiveSatellite;
        break;
      case 'n':
        toReturn = PartOfSpeech.Noun;
        break;
      case 'v':
        toReturn = PartOfSpeech.Verb;
        break;
      case 'r':
        toReturn = PartOfSpeech.Adverb;
        break;
    }
    if (toReturn == null) {
      throw new IllegalArgumentException(String.valueOf(key));
    }
    return toReturn;
  }
}


package com.wishitventures.nlp.domain;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Dictionary {

    private final Map<String, Word> textToWords = new HashMap<String, Word>();
    private final static Dictionary instance = new Dictionary();

    private Dictionary() {
        //DictionaryLoader loader = new DefaultEnglishDictionaryLoader(new File("/Users/ECA/Desktop/Search_Project/Dictionaries/scowl-2014.11.17/parts_of_speech"));
        DictionaryLoader loader = new WordSenseDictionaryLoader(new File(WordSenseDictionaryLoader.WORDSENSE_DICTIONARY_PATH));

        try {
            for (Word word : loader.loadWords().words()) {
                textToWords.put(word.rawValue(), word);
            }
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    public Map<String, Word> textToWords() {
        return Collections.unmodifiableMap(textToWords);
    }

    public Word fromChars(String chars) {
        return textToWords.get(chars);
    }

    public boolean contains(String chars) {
        return textToWords.containsKey(chars);
    }

    public static final Dictionary instance() {
        return instance;
    }

    public interface DictionaryLoader {

        WordsCollectionFactory.WordsCollector loadWords() throws Exception;
    }

}
